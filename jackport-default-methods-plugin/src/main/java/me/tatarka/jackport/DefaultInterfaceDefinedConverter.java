package me.tatarka.jackport;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPrimitiveType;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JThis;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.ir.sourceinfo.SourceInfoFactory;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.JLookup;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.NamingTools;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.ExclusiveAccess;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import me.tatarka.jackport.filter.DefaultInterfaceFilter;
import me.tatarka.jackport.marker.DefaultMethodImpl;
import me.tatarka.jackport.marker.DefaultMethodInterface;
import me.tatarka.jackport.transform.PrintSource;
import me.tatarka.jackport.transform.RemoveMethod;
import me.tatarka.jackport.transform.ReplaceMethod;
import me.tatarka.jackport.util.Util;

@Description("Convert default methods in interface to abstract class")
@Constraint(need = {JDefinedInterface.class})
@Transform(add = {JDefinedClass.class, JMethod.class}, modify = {JMethod.class})
@Filter(DefaultInterfaceFilter.class)
@ExclusiveAccess(JSession.class)
@Synchronized
public class DefaultInterfaceDefinedConverter implements RunnableSchedulable<JDefinedClassOrInterface> {

    @Nonnull
    private final JLookup lookup = Jack.getSession().getLookup();

    private class InterfaceDefaultToAbstractConverter extends JVisitor {
        @Nonnull
        private final TransformationRequest tr;
        @Nonnull
        private final String abstractClassName;

        @Nonnull
        private final List<JMethod> defaultMethods = new ArrayList<>();

        @Nonnull
        private final List<JMethod> staticMethods = new ArrayList<>();

        InterfaceDefaultToAbstractConverter(@Nonnull TransformationRequest tr) {
            this.tr = tr;
            abstractClassName = NamingTools.getNonSourceConflictingName("DefaultImpl");
        }

        @Override
        public boolean visit(@Nonnull JMethod method) {
            if (method.isAbstract() || JMethod.isClinit(method)) {
                return false;
            }

            if (method.isStatic()) {
                staticMethods.add(method);

                tr.append(new RemoveMethod(method));
            } else {
                defaultMethods.add(method);

                JMethod abstractMethod = new JMethod(
                        method.getSourceInfo(),
                        method.getMethodId(),
                        method.getEnclosingType(),
                        method.getModifier() | JModifier.ABSTRACT
                );
                for (JParameter param : method.getParams()) {
                    abstractMethod.addParam(param);
                }

                tr.append(new ReplaceMethod(method, abstractMethod));
            }

            return false;
        }

        @Override
        public void endVisit(@Nonnull JDefinedInterface definedInterface) {
            JPackage javaLang = new JPackage("lang", new JPackage("java", new JPackage("", null)));
            JClass object = new JDefinedClass(SourceInfo.UNKNOWN, "Object", JModifier.PUBLIC, javaLang, NopClassOrInterfaceLoader.INSTANCE);

            String simpleName = definedInterface.getName() + "$" + abstractClassName;
            definedInterface.addMarker(new DefaultMethodInterface(simpleName));

            int visibility = definedInterface.getModifier() & (JModifier.PUBLIC | JModifier.PRIVATE);
            JDefinedClass defaultImpl = new JDefinedClass(
                    new SourceInfoFactory().create(definedInterface.getSourceInfo().getFileName()),
                    simpleName, visibility | JModifier.ABSTRACT | JModifier.STATIC,
                    definedInterface.getEnclosingPackage(), NopClassOrInterfaceLoader.INSTANCE
            );

            defaultImpl.setEnclosingType(definedInterface);
            defaultImpl.setSuperClass(object);
            defaultImpl.addImplements(definedInterface);
            defaultImpl.addMarker(new SimpleName(simpleName));
            defaultImpl.addMarker(new DefaultMethodImpl(simpleName));

            int modifiers = definedInterface.getModifier() & (JModifier.PUBLIC | JModifier.PRIVATE);
            JConstructor constructor = new JConstructor(SourceInfo.UNKNOWN, defaultImpl, modifiers);
            JBlock block = new JBlock(SourceInfo.UNKNOWN);
            JMethodIdWide id = defaultImpl.getOrCreateMethodIdWide("<init>", Collections.<JType>emptyList(), MethodKind.INSTANCE_NON_VIRTUAL);
            JMethodCall call = new JMethodCall(SourceInfo.UNKNOWN, new JThis(constructor).makeRef(SourceInfo.UNKNOWN), object, id, JPrimitiveType.JPrimitiveTypeEnum.VOID.getType(), true);
            call.addArgs(Collections.<JExpression>emptyList());
            JStatement stmt = new JExpressionStatement(SourceInfo.UNKNOWN, call);
            block.addStmt(stmt);
            JStatement retStmt = new JReturnStatement(SourceInfo.UNKNOWN, null);
            block.addStmt(retStmt);
            JMethodBody body = new JMethodBody(SourceInfo.UNKNOWN, block);
            constructor.setBody(body);

            tr.append(new AppendMethod(defaultImpl, constructor));

            for (JMethod method : defaultMethods) {
                appendMethod(defaultImpl, method);
                System.out.println("-" + Util.className(definedInterface) + "." + method.getName() + "()");
            }
            for (JMethod method : staticMethods) {
                appendMethod(defaultImpl, method);
                System.out.println("-" + Util.className(definedInterface) + "." + method.getName() + "()");
            }

            Jack.getSession().addTypeToEmit(defaultImpl);

            System.out.println("+" + Util.className(defaultImpl));
            if (definedInterface.getName().equals("IntConsumer")) {
                tr.append(new PrintSource(definedInterface));
            }
        }

        private void appendMethod(JDefinedClass defaultImpl, JMethod method) {
            List<JParameter> params = method.getParams();
            List<JType> argTypes = new ArrayList<>(params.size());
            for (JParameter param : params) {
                argTypes.add(param.getType());
            }
            JMethodId id = defaultImpl.getOrCreateMethodId(
                    method.getName(),
                    argTypes,
                    MethodKind.INSTANCE_VIRTUAL,
                    method.getType()
            );
            JMethod implMethod = new JMethod(
                    method.getSourceInfo(),
                    id,
                    defaultImpl,
                    method.getModifier()
            );
            for (JParameter param : params) {
                implMethod.addParam(param);
            }
            implMethod.setBody(method.getBody());
            tr.append(new AppendMethod(defaultImpl, implMethod));
        }
    }

    @Override
    public void run(@Nonnull JDefinedClassOrInterface type) {
        TransformationRequest request = new TransformationRequest(type);
        InterfaceDefaultToAbstractConverter visitor = new InterfaceDefaultToAbstractConverter(request);
        visitor.accept(type);
        request.commit();
    }
}
