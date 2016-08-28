package me.tatarka.jackport;

import com.android.jack.ir.HierarchyFilter;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodIdWide;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPhantomClass;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.JLookup;
import com.android.jack.lookup.JNodeLookup;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

@Description("Backports base java 8 api methods")
@Produce(BackportJava8Api.class)
public class BackportBaseClasses implements RunnableSchedulable<JDefinedClassOrInterface> {

    private static String[][] CLASSES = classes(
            "java.lang.FunctionalInterface",
            "java.util.Objects",
            "java.util.function.*"
    );

    private ArrayList<String> seen = new ArrayList<>();
    private JLookup lookup;

    @Override
    public void run(@Nonnull final JDefinedClassOrInterface type) {
        lookup = new JNodeLookup(new JPackage("", null));
        final TransformationRequest tr = new TransformationRequest(type);
        type.traverse(new JVisitor() {
            @Override
            public boolean visit(@Nonnull final JDefinedClassOrInterface type) {
                if (checkType(type.getSuperClass()) == Type.NEW) {
                    tr.append(new ChangePackage(type.getSuperClass()));
                }
                for (JInterface interfaceType : type.getImplements()) {
                    Type t = checkType(interfaceType);
                    if (t == Type.NEW) {
                        tr.append(new ChangePackage(interfaceType));
                    }
                    if (type instanceof JDefinedClass && t != Type.IGNORE) {
                        tr.append(new InsertDefaultMethods((JDefinedClass) type, (JDefinedInterface) interfaceType));
                    }
                }
                return super.visit(type);
            }

            @Override
            public boolean visit(@Nonnull JField field) {
                if (checkType(field.getType()) == Type.NEW) {
                    tr.append(new ChangePackage((JClassOrInterface) field.getType()));
                }
                return super.visit(field);
            }

            @Override
            public boolean visit(@Nonnull JMethod method) {
                if (checkType(method.getType()) == Type.NEW) {
                    tr.append(new ChangePackage((JClassOrInterface) method.getType()));
                }
                for (JParameter param : method.getParams()) {
                    if (checkType(param.getType()) == Type.NEW) {
                        tr.append(new ChangePackage((JClassOrInterface) param.getType()));
                    }
                }
                return super.visit(method);
            }

            @Override
            public boolean visit(@Nonnull JMethodCall methodCall) {
                Type t = checkType(methodCall.getReceiverType());
                if (t == Type.NEW) {
                    tr.append(new ChangePackage(methodCall.getReceiverType()));
                }
                if (t != Type.IGNORE) {
                    if (methodCall.getReceiverType() instanceof JInterface && methodCall.getDispatchKind() == JMethodCall.DispatchKind.DIRECT) {
                        tr.append(new CallInterfaceStaticMethod(methodCall));
                    }
                }
                return super.visit(methodCall);
            }

            @Override
            public boolean visit(@Nonnull JLambda lambda) {
                if (checkType(lambda.getType()) == Type.NEW) {
                    tr.append(new ChangePackage(lambda.getType()));
                }
                return super.visit(lambda);
            }

            @Override
            public boolean visit(@Nonnull JClassLiteral classLiteral) {
                if (checkType(classLiteral.getRefType()) == Type.NEW) {
                    tr.append(new ChangePackage((JClassOrInterface) classLiteral.getRefType()));
                }
                return super.visit(classLiteral);
            }
        });
        tr.commit();
    }

    private Type checkType(JType component) {
        if (!(component instanceof JClassOrInterface)) {
            return Type.IGNORE;
        }
        JClassOrInterface type = (JClassOrInterface) component;
        if (seen.contains(stripJackportClassName(type))) {
            return Type.SEEN;
        }
        String className = className(type);
        boolean result = classMatches(className);
        if (result) {
            seen.add(className);
        }
        return result ? Type.NEW : Type.IGNORE;
    }

    private static String[][] classes(String... args) {
        String[][] result = new String[args.length][];
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            result[i] = arg.split("\\.");
        }
        return result;
    }

    private static boolean classMatches(String targetClassName) {
        String[] targetParts = targetClassName.split("\\.");
        loop:
        for (String[] classParts : CLASSES) {
            if (targetParts.length != classParts.length) {
                continue;
            }
            for (int i = 0; i < classParts.length; i++) {
                if (classParts[i].equals("*")) {
                    continue;
                }
                if (!targetParts[i].equals(classParts[i])) {
                    continue loop;
                }
            }
            return true;
        }
        return false;
    }
    
    private static JPackage withJackport(JPackage jPackage) {
        if (jPackage.isTopLevelPackage()) {
            return new JPackage("jackport", jPackage);
        } else {
            return new JPackage(jPackage.getName(), withJackport(jPackage.getEnclosingPackage()));
        }
    }

    private static class ChangePackage implements TransformationStep {
        final JClassOrInterface type;

        ChangePackage(JClassOrInterface type) {
            this.type = type;
        }

        @Override
        public void apply() throws UnsupportedOperationException {
            String oldName = stripJackportClassName(type);
            type.setEnclosingPackage(withJackport(type.getEnclosingPackage()));
            String newName = className(type);
            System.out.println(oldName + " -> " + newName);
        }
    }

    private static class CallInterfaceStaticMethod implements TransformationStep {
        final JMethodCall call;

        CallInterfaceStaticMethod(JMethodCall call) {
            this.call = call;
        }

        @Override
        public void apply() throws UnsupportedOperationException {
            JClassOrInterface oldReceiver = call.getReceiverType();
            JClassOrInterface newReceiver = new JPhantomClass(oldReceiver.getName() + "$$", oldReceiver.getEnclosingPackage());
            JMethodCall newMethodCall = new JMethodCall(
                    call.getSourceInfo(),
                    call.getInstance(),
                    newReceiver,
                    call.getMethodId(),
                    call.getType(),
                    /*virtualDispatch=*/false);
            call.getParent().replace(call, newMethodCall);
            System.out.println(stipJackportMethodName(call) + " -> " + methodName(newMethodCall));
        }
    }

    private static class InsertDefaultMethods implements TransformationStep {
        final JDefinedClass type;
        final JDefinedInterface interfaceType;

        private InsertDefaultMethods(JDefinedClass type, JDefinedInterface interfaceType) {
            this.type = type;
            this.interfaceType = interfaceType;
        }

        @Override
        public void apply() throws UnsupportedOperationException {
//            JClass superclass = type.getSuperClass();
//            if (className(superclass).equals("java.lang.Object")) {
//                insertBySubclass();
//            } else {
                insertByMethodGeneration();
//            }
        }

        private void insertBySubclass() {
            JClass superclass = new JPhantomClass(interfaceType.getName() + "$$", interfaceType.getEnclosingPackage());
            type.setSuperClass(superclass);
            type.remove(interfaceType);
            System.out.println("changed " + className(type) + " supperclass to " + className(superclass));
            System.out.println(type.toSource());
        }

        private void insertByMethodGeneration() {
            for (JMethod method : interfaceType.getMethods()) {
                if (!method.isAbstract() && !method.isStatic()) {
                    List<JParameter> params = method.getParams();
                    List<JType> args = new ArrayList<>(params.size());
                    for (JParameter param : params) {
                        args.add(param.getType());
                    }
                    JMethodId methodId = type.getOrCreateMethodId(method.getName(), args, MethodKind.INSTANCE_VIRTUAL, method.getType());
                    Collection<JMethod> methods = methodId.getMethods(type, HierarchyFilter.THIS_TYPE);
                    if (methods.isEmpty()) {
                        JMethod newMethod = new JMethod(SourceInfo.UNKNOWN, methodId, type, method.getModifier());
                        for (JParameter param : params) {
                            newMethod.addParam(param);
                        }
                        JBlock block = new JBlock(SourceInfo.UNKNOWN);
                        JMethodBody methodBody = new JMethodBody(SourceInfo.UNKNOWN, block);

                        JClass receiver = new JPhantomClass(interfaceType.getName() + "$$", interfaceType.getEnclosingPackage());
                        List<JType> callArgsTypes = new ArrayList<>(args.size() + 1);
                        callArgsTypes.add(interfaceType);
                        callArgsTypes.addAll(args);
                        JMethodIdWide receiverMethodId = receiver.getMethodIdWide(method.getName(), callArgsTypes, MethodKind.STATIC);
                        JMethodCall call = new JMethodCall(SourceInfo.UNKNOWN, null, receiver, receiverMethodId, method.getType(), /*virtualDispatch=*/false);
                        call.addArg(newMethod.getThis().makeRef(SourceInfo.UNKNOWN));
                        for (JParameter param : params) {
                            call.addArg(param.makeRef(SourceInfo.UNKNOWN));
                        }
                        JReturnStatement statement = new JReturnStatement(SourceInfo.UNKNOWN, call);
                        block.addStmt(statement);
                        newMethod.setBody(methodBody);
                        type.addMethod(newMethod);
                        newMethod.updateParents(type);
                        System.out.println("created method " + methodName(newMethod));
                    }
                }
            }
        }
    }

    private static String className(JClassOrInterface type) {
        if (type == null) {
            return null;
        }
        return type.getEnclosingPackage().toString() + "." + type.getName();
    }

    private static String stripJackportClassName(JClassOrInterface type) {
        String className = className(type);
        // Type might have already been converted, if so strip jackport prefix.
        if (className.startsWith("jackport")) {
            className = className.substring("jackport.".length());
        }
        return className;
    }

    private static String methodName(JMethodCall call) {
        if (call == null) {
            return null;
        }
        return className(call.getReceiverType()) + "." + call.getMethodName();
    }

    private static String stipJackportMethodName(JMethodCall call) {
        if (call == null) {
            return null;
        }
        return stripJackportClassName(call.getReceiverType()) + "." + call.getMethodName();
    }

    private static String methodName(JMethod method) {
        if (method == null) {
            return null;
        }
        return className(method.getEnclosingType()) + "." + method.getName();
    }

    private enum Type {
        IGNORE, NEW, SEEN
    }
}
