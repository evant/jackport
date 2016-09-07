package me.tatarka.jackport.marker;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.formatter.BinarySignatureFormatter;
import com.android.jack.ir.formatter.TypeAndMethodFormatter;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.NopClassOrInterfaceLoader;
import com.android.jack.lookup.JLookup;
import com.android.jack.shrob.spec.ModifierSpecification;
import com.android.sched.item.Description;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

@Description("Marks an interface as holding default methods that are backported")
@ValidOn(JDefinedInterface.class)
public class DefaultMethodInterface extends SimpleName {
    public DefaultMethodInterface(@Nonnull String simpleName) {
        super(simpleName);
    }

    public JDefinedClass lookup(JInterface targetInterface) {
        return new JDefinedClass(targetInterface.getSourceInfo(), getSimpleName(), JModifier.PUBLIC, targetInterface.getEnclosingPackage(), NopClassOrInterfaceLoader.INSTANCE);
    }
}
