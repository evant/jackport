package me.tatarka.jackport.transform;

import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

public class ChangeSuperClass implements TransformationStep, TransformStep {
    @Nonnull
    private final JDefinedClass definedClass;
    @Nonnull
    private final JClass superClass;

    public ChangeSuperClass(@Nonnull JDefinedClass definedClass, @Nonnull JClass superClass) {
        this.definedClass = definedClass;
        this.superClass = superClass;
    }

    public void apply() throws UnsupportedOperationException {
        definedClass.setSuperClass(superClass);

        // We need to fix up super call to the new super class.
        for (final JMethod method : definedClass.getMethods()) {
            if (method.getName().equals("<init>")) {
                method.traverse(new JVisitor() {
                    @Override
                    public boolean visit(@Nonnull JMethodCall methodCall) {
                        if (methodCall.getMethodName().equals("<init>")) {
                            ReplaceReceiverMethodCall newMethodCall = new ReplaceReceiverMethodCall(methodCall, superClass);
                            methodCall.getParent().replace(methodCall, newMethodCall);
                            newMethodCall.updateParents(methodCall.getParent());
                        }
                        return super.visit(methodCall);
                    }
                });
            }
        }
    }

    @Nonnull
    public String toString() {
        return "Change SuperClass of " + definedClass.toString() + " to " + superClass.toString();
    }

    private static class ReplaceReceiverMethodCall extends JMethodCall {
        public ReplaceReceiverMethodCall(@Nonnull JMethodCall other, JClassOrInterface recevierType) {
            super(other, other.getInstance());
            setReceiverType(recevierType);
        }

        @Override
        protected void setReceiverType(@Nonnull JClassOrInterface receiverType) {
            super.setReceiverType(receiverType);
        }
    }
}