package me.tatarka.jackport;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.JLookup;
import com.android.jack.transformations.request.AppendMethod;
import com.android.jack.transformations.request.ChangeSuperClass;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Filter;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import me.tatarka.jackport.filter.ClassFilter;
import me.tatarka.jackport.marker.DefaultMethodImpl;
import me.tatarka.jackport.marker.DefaultMethodInterface;
import me.tatarka.jackport.transform.RemoveImplements;
import me.tatarka.jackport.util.Util;

@Description("Convert implementations of interfaces with default methods to subclasses")
@Constraint(need = {JDefinedClass.class, DefaultMethodInterface.class}, no = {JLambda.class})
@Transform(add = {JMethod.class}, modify = {JDefinedClass.class})
@Filter(ClassFilter.class)
public class DefaultInterfaceUsageConverter implements RunnableSchedulable<JDefinedClassOrInterface> {

    @Nonnull
    private final JLookup lookup = Jack.getSession().getLookup();

    private class ImplementsToExtendsConverter extends JVisitor {
        @Nonnull
        private final TransformationRequest tr;

        ImplementsToExtendsConverter(@Nonnull TransformationRequest tr) {
            this.tr = tr;
        }

        @Override
        public boolean visit(@Nonnull JDefinedClass definedClass) {
            if (definedClass.containsMarker(DefaultMethodImpl.class)) {
                return false;
            }
            JInterface targetInterface = null;
            DefaultMethodInterface marker = null;
            for (JInterface iface : definedClass.getImplements()) {
                marker = Util.getMarker(iface, DefaultMethodInterface.class);
                if (marker != null) {
                    targetInterface = iface;
                    break;
                }
            }

            if (targetInterface != null) {
                JPackage javaLang = new JPackage("lang", new JPackage("java", new JPackage("", null)));
                JClass object = new JDefinedClass(SourceInfo.UNKNOWN, "Object", JModifier.PUBLIC, javaLang, NopClassOrInterfaceLoader.INSTANCE);
                JDefinedClass defaultClass = marker.lookup(targetInterface);
                tr.append(new RemoveImplements(definedClass, targetInterface));
                if (definedClass.getSuperClass().isSameType(object)) {
                    // Easy, replace the superclass with our abstract impl
                    tr.append(new ChangeSuperClass(definedClass, defaultClass));
                    System.out.println("implements " + Util.className(definedClass.getSuperClass()) + " -> extends " + Util.className(defaultClass));
                } else {
                    // Harder, copy default impls where they aren't overridden. 
                    for (JMethod defaultMethod : defaultClass.getMethods()) {
                        if (JMethod.isClinit(defaultMethod) || defaultMethod.isStatic()) {
                            continue;
                        }

                        if (!containsMethod(definedClass, defaultMethod)) {
                            List<JParameter> params = defaultMethod.getParams();
                            List<JType> argTypes = new ArrayList<>(params.size());
                            for (JParameter param : params) {
                                argTypes.add(param.getType());
                            }
                            JMethodId id = definedClass.getOrCreateMethodId(
                                    defaultMethod.getName(),
                                    argTypes,
                                    MethodKind.INSTANCE_VIRTUAL,
                                    defaultMethod.getType()
                            );
                            JMethod implMethod = new JMethod(
                                    defaultMethod.getSourceInfo(),
                                    id,
                                    definedClass,
                                    defaultMethod.getModifier()
                            );
                            for (JParameter param : params) {
                                implMethod.addParam(param);
                            }
                            implMethod.setBody(defaultMethod.getBody());
                            tr.append(new AppendMethod(definedClass, implMethod));
                            System.out.println("+" + Util.className(definedClass) + "." + defaultMethod.getName() + "()");
                        }
                    }
                }
            }

            return false;
        }
    }

    private static boolean containsMethod(JDefinedClass definedClass, JMethod targetMethod) {
        loop:
        for (JMethod method : definedClass.getMethods()) {
            if (!method.getName().equals(targetMethod.getName())) {
                continue;
            }

            List<JParameter> mParams = method.getParams();
            List<JParameter> tParams = targetMethod.getParams();

            if (mParams.size() != tParams.size()) {
                continue;
            }

            for (int i = 0; i < mParams.size(); i++) {
                JParameter mParam = mParams.get(i);
                JParameter tParam = tParams.get(i);
                if (!mParam.getType().isSameType(tParam.getType())) {
                    continue loop;
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void run(@Nonnull JDefinedClassOrInterface type) {
        TransformationRequest request = new TransformationRequest(type);
        ImplementsToExtendsConverter visitor = new ImplementsToExtendsConverter(request);
        visitor.accept(type);
        request.commit();
    }
}
