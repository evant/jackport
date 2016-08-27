package me.tatarka.jackport;

import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JLambda;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPhantomClass;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;

import java.util.ArrayList;

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

    @Override
    public void run(@Nonnull final JDefinedClassOrInterface type) {
        final TransformationRequest tr = new TransformationRequest(type);
        type.traverse(new JVisitor() {
            @Override
            public boolean visit(@Nonnull final JDefinedClassOrInterface type) {
                processChangePackage(type.getSuperClass(), tr);
                for (JInterface interfaceType : type.getImplements()) {
                    processChangePackage(interfaceType, tr);
                }
                return super.visit(type);
            }

            @Override
            public boolean visit(@Nonnull JMethod method) {
                processChangePackage(method.getType(), tr);
                for (JParameter param : method.getParams()) {
                    processChangePackage(param.getType(), tr);
                }
                return super.visit(method);
            }

            @Override
            public boolean visit(@Nonnull JMethodCall methodCall) {
                if (methodCall.getReceiverType() instanceof JInterface && methodCall.getDispatchKind() == JMethodCall.DispatchKind.DIRECT) {
                    processStaticInterfaceMethod(methodCall, tr);
                } else {
                    processChangePackage(methodCall.getReceiverType(), tr);
                }
                return super.visit(methodCall);
            }

            @Override
            public boolean visit(@Nonnull JLambda lambda) {
                processChangePackage(lambda.getType(), tr);
                return super.visit(lambda);
            }

            @Override
            public boolean visit(@Nonnull JClassLiteral classLiteral) {
                processChangePackage(classLiteral.getRefType(), tr);
                return super.visit(classLiteral);
            }
        });
        tr.commit();
    }

    private void processChangePackage(JType type, TransformationRequest tr) {
        if (checkType(type)) {
            tr.append(new ChangePackage((JClassOrInterface) type));
        }
    }

    private void processStaticInterfaceMethod(JMethodCall call, TransformationRequest tr) {
        JClassOrInterface type = call.getReceiverType();
        if (checkType(type)) {
            tr.append(new ChangePackage(type));
            tr.append(new CallInterfaceStaticMethod(call));
        }
    }

    private boolean checkType(JType component) {
        if (!(component instanceof JClassOrInterface)) {
            return false;
        }
        JClassOrInterface type = (JClassOrInterface) component;
        String className = className(type);
        if (seen.contains(className)) {
            return false;
        }
        boolean result = classMatches(className);
        if (result) {
            seen.add(className);
        }
        return result;
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
            String oldName = className(type);
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
            System.out.println(methodName(call) + " -> " + methodName(newMethodCall));
        }
    }

    private static String className(JClassOrInterface type) {
        if (type == null) {
            return null;
        }
        return type.getEnclosingPackage().toString() + "." + type.getName();
    }
    
    private static String methodName(JMethodCall call) {
        if (call == null) {
            return null;
        }
        return className(call.getReceiverType()) + "." + call.getMethodName();
    }
}
