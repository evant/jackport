package me.tatarka.jackport.transform;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.transform.TransformStep;

import javax.annotation.Nonnull;

public class ReplaceMethod implements TransformationStep, TransformStep {
    @Nonnull
    private final JMethod existingMethod;
    @Nonnull
    private final JMethod newMethod;

    public ReplaceMethod(@Nonnull JMethod existingMethod, @Nonnull JMethod newMethod) {
        this.existingMethod = existingMethod;
        this.newMethod = newMethod;
    }

    @Override
    public void apply() throws UnsupportedOperationException {
        JDefinedClassOrInterface type = existingMethod.getEnclosingType();
        type.remove(existingMethod);
        type.addMethod(newMethod);
        newMethod.updateParents(type);
    }

    @Nonnull
    public String toString() {
        StringBuilder sb = new StringBuilder("Replace ");
        sb.append(existingMethod.toSource());
        sb.append(" with ");
        sb.append(newMethod.toSource());
        sb.append(" in ");
        sb.append(existingMethod.getParent().toSource());
        return sb.toString();
    }
}
