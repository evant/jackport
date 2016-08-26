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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

@Description("Backports base java 8 api methods")
@Produce(BackportJava8Api.class)
public class BackportBaseClasses implements RunnableSchedulable<JDefinedClassOrInterface> {

    private static List<String> CLASSES = Arrays.asList(
            "java.lang.FunctionalInterface",
            "java.util.Objects",
            "java.util.function.Predicate",
            "java.util.function.Supplier"
    );

    private ArrayList<String> seen = new ArrayList<>();

    @Override
    public void run(@Nonnull final JDefinedClassOrInterface type) {
        final TransformationRequest tr = new TransformationRequest(type);
        type.traverse(new JVisitor() {
            @Override
            public boolean visit(@Nonnull final JDefinedClassOrInterface type) {
                process(type, tr);
                return super.visit(type);
            }

            @Override
            public boolean visit(@Nonnull JMethodCall methodCall) {
                process(methodCall.getReceiverType(), tr);
                return super.visit(methodCall);
            }
        });
        tr.commit();
    }

    private void process(final JClassOrInterface type, TransformationRequest tr) {
        if (checkClass(type)) {
            tr.append(new TransformationStep() {
                @Override
                public void apply() throws UnsupportedOperationException {
                    type.setEnclosingPackage(withJackport(type.getEnclosingPackage()));
                }
            });
        }
    }

    private boolean checkClass(JClassOrInterface type) {
        String className = type.getEnclosingPackage().toString() + "." + type.getName();
        if (seen.contains(className)) {
            return false;
        }
        boolean result = CLASSES.contains(className);
        if (result) {
            seen.add(className);
        }
        return result;
    }

    private static JPackage withJackport(JPackage jPackage) {
        if (jPackage.isTopLevelPackage()) {
            return new JPackage("jackport", jPackage);
        } else {
            return new JPackage(jPackage.getName(), withJackport(jPackage.getEnclosingPackage()));
        }
    }
}
