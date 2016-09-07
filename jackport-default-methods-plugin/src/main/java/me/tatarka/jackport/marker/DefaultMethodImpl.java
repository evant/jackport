package me.tatarka.jackport.marker;

import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.sched.item.Description;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

@Description("Marks the generated impl for interface default methods")
@ValidOn(JDefinedInterface.class)
public class DefaultMethodImpl extends SimpleName {
    public DefaultMethodImpl(@Nonnull String simpleName) {
        super(simpleName);
    }
}
