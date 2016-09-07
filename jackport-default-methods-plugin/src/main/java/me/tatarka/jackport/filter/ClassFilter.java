package me.tatarka.jackport.filter;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.schedulable.ComponentFilter;

import javax.annotation.Nonnull;

@Description("filters only classes and not interfaces")
public class ClassFilter implements ComponentFilter<JDefinedClassOrInterface> {
    @Override
    public boolean accept(@Nonnull JDefinedClassOrInterface type) {
        return type instanceof JDefinedClass;
    }
}
