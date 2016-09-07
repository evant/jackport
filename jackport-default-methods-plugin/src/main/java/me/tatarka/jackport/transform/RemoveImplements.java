package me.tatarka.jackport.transform;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.transformations.request.TransformationStep;
import com.android.sched.transform.TransformStep;

import java.util.List;

import javax.annotation.Nonnull;

public class RemoveImplements implements TransformationStep, TransformStep {
    @Nonnull
    private final JDefinedClass definedClass;
    @Nonnull
    private final JInterface iface;

    public RemoveImplements(@Nonnull JDefinedClass definedClass, @Nonnull JInterface iface) {
        this.definedClass = definedClass;
        this.iface = iface;
    }

    public void apply() throws UnsupportedOperationException {
        List<JInterface> interfaces = definedClass.getImplements();
        interfaces.remove(iface);
        definedClass.setImplements(interfaces);
    }

    @Nonnull
    public String toString() {
        StringBuilder sb = new StringBuilder("remove implements of ");
        sb.append(iface.toString());
        sb.append(" to ");
        sb.append(definedClass.toString());
        return sb.toString();
    }
}