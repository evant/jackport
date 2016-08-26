package me.tatarka.jackport;

import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;

import javax.annotation.Nonnull;

@Description("Backports Objects api methods")
@Produce(BackportJava8Api.class)
public class BackportObjectsMethods implements RunnableSchedulable<JDefinedClassOrInterface> {
    @Override
    public void run(@Nonnull JDefinedClassOrInterface type) {
        final TransformationRequest tr = new TransformationRequest(type);
        type.traverse(new JVisitor() {
            @Override
            public boolean visit(@Nonnull JMethodCall call) {
                JClassOrInterface receiver = call.getReceiverType();
                if (!receiver.getEnclosingPackage().toString().equals("java.util")) {
                    return super.visit(call);
                }
                if (!receiver.getName().equals("Objects")) {
                    return super.visit(call);
                }
                switch (call.getMethodName()) {
                    case "equals":
                        convertEquals(call, tr);
                        break;
                }
                return super.visit(call);
            }
        });
        tr.commit();
    }

    private void convertEquals(final JMethodCall call, TransformationRequest tr) {
        tr.append(new TransformationStep() {
            @Override
            public void apply() throws UnsupportedOperationException {
                call.getReceiverType().setEnclosingPackage(withJackport(call.getReceiverType().getEnclosingPackage()));
            }
        });
    }

    private static JPackage withJackport(JPackage jPackage) {
        if (jPackage.isTopLevelPackage()) {
            return new JPackage("jackport", jPackage);
        } else {
            return new JPackage(jPackage.getName(), withJackport(jPackage.getEnclosingPackage()));
        }
    }
}
