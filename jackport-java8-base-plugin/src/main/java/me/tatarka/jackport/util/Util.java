package me.tatarka.jackport.util;

import com.android.jack.ir.ast.HasEnclosingPackage;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.sched.marker.Marker;

public class Util {
    
    public static <T extends JType & HasEnclosingPackage> String className(T type) {
        return type.getEnclosingPackage() + "." + type.getName();
    }
}
