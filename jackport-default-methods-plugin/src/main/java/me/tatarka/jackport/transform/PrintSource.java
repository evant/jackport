package me.tatarka.jackport.transform;

import com.android.jack.ir.ast.JNode;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

public class PrintSource implements TransformationStep, TransformStep {

    @Nonnull
    private final JNode node;

    public PrintSource(@Nonnull JNode node) {
        this.node = node;
    }

    @Override
    public void apply() throws UnsupportedOperationException {
        System.out.println(node.toSource());
    }
}
