package me.tatarka.jackport;

import com.android.jack.ir.ast.HasEnclosingPackage;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

public class ChangePackage implements TransformationStep, TransformStep {

    @Nonnull
    private final HasEnclosingPackage node;
    @Nonnull
    private final JPackage newPackage;

    public ChangePackage(@Nonnull HasEnclosingPackage node, @Nonnull JPackage newPackage) {
        this.node = node;
        this.newPackage = newPackage;
    }

    @Override
    public void apply() throws UnsupportedOperationException {
        node.setEnclosingPackage(newPackage);
    }
}
