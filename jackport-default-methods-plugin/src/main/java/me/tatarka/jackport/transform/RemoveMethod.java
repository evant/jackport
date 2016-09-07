package me.tatarka.jackport.transform;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

public class RemoveMethod implements TransformationStep, TransformStep {
    @Nonnull
    private final JMethod existingMethod;

    public RemoveMethod(@Nonnull JMethod existingMethod) {
        this.existingMethod = existingMethod;
    }

    @Override
    public void apply() throws UnsupportedOperationException {
        JDefinedClassOrInterface type = existingMethod.getEnclosingType();
        type.remove(existingMethod);
    }

    @Nonnull
    public String toString() {
        StringBuilder sb = new StringBuilder("Remove ");
        sb.append(existingMethod.toSource());
        sb.append(" in ");
        sb.append(existingMethod.getParent().toSource());
        return sb.toString();
    }
}
