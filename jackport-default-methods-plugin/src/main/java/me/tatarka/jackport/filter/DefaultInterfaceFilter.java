package me.tatarka.jackport.filter;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JPackage;
import com.android.sched.item.Description;
import com.android.sched.schedulable.ComponentFilter;

import javax.annotation.Nonnull;

@Description("Filter accepting interfaces with default methods")
public class DefaultInterfaceFilter implements ComponentFilter<JDefinedClassOrInterface> {

    @Override
    public boolean accept(@Nonnull JDefinedClassOrInterface type) {
        if (type instanceof JDefinedInterface) {
            for (JMethod method : type.getMethods()) {
                if (!method.isAbstract() && !JMethod.isClinit(method) && !method.isSynthetic() && !isSystem(type.getEnclosingPackage())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isSystem(JPackage pkg) {
        String name = pkg.toSource();
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.");
    }
}
