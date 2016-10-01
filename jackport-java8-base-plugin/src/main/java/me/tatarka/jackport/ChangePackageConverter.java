package me.tatarka.jackport;

import com.android.jack.ir.ast.HasEnclosingPackage;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.ChangeEnclosingPackage;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Nonnull;

import me.tatarka.jackport.util.Util;

@Description("changes packages of java 8 classes to backported version")
@Constraint(need = {JDefinedClassOrInterface.class})
@Transform(modify = {JPackage.class})
public class ChangePackageConverter implements RunnableSchedulable<JDefinedClassOrInterface> {
    private static String[][] CLASSES = classes(
            "java.lang.FunctionalInterface",
            "java.util.function.*",
            "java.util.Objects",
            "java.util.Optional",
            "java.util.OptionalInt",
            "java.util.OptionalLong",
            "java.util.OptionalDouble",
            "java.util.Iterator",
            "java.util.PrimativeIterator",
            "java.util.Spilterator",
            "java.util.Spilterators",
            "java.util.StringJoiner",
            "java.util.IntSummaryStatistics",
            "java.util.LongSummaryStatistics",
            "java.util.DoubleSummaryStatistics"
    );

    private ArrayList<String> seen = new ArrayList<>();

    private static String[][] classes(String... args) {
        String[][] result = new String[args.length][];
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            result[i] = arg.split("\\.");
        }
        return result;
    }

    private static boolean classMatches(String targetClassName) {
        String[] targetParts = targetClassName.split("\\.");
        loop:
        for (String[] classParts : CLASSES) {
            if (targetParts.length != classParts.length) {
                continue;
            }
            for (int i = 0; i < classParts.length; i++) {
                if (classParts[i].equals("*")) {
                    continue;
                }
                if (!targetParts[i].equals(classParts[i])) {
                    continue loop;
                }
            }
            return true;
        }
        return false;
    }

    private boolean checkType(JType component) {
        if (!(component instanceof JClassOrInterface)) {
            return false;
        }
        JClassOrInterface type = (JClassOrInterface) component;
        if (seen.contains(stripJackportClassName(type))) {
            return false;
        }
        String className = className(type);
        boolean result = classMatches(className);
        if (result) {
            seen.add(className);
        }
        return result;
    }

    private static String className(JClassOrInterface type) {
        if (type == null) {
            return null;
        }
        return type.getEnclosingPackage().toString() + "." + type.getName();
    }

    private static String stripJackportClassName(JClassOrInterface type) {
        String className = className(type);
        // Type might have already been converted, if so strip jackport prefix.
        if (className.startsWith("jackport")) {
            className = className.substring("jackport.".length());
        }
        return className;
    }

    private static JPackage withJackport(JPackage jPackage) {
        if (jPackage.isTopLevelPackage()) {
            return new JPackage("jackport", jPackage);
        } else {
            return new JPackage(jPackage.getName(), withJackport(jPackage.getEnclosingPackage()));
        }
    }

    @Override
    public void run(@Nonnull final JDefinedClassOrInterface type) {
        if (type.getEnclosingPackage().toString().startsWith("jackport")) {
            // Don't backported backported classes.
            return;
        }
        final TransformationRequest tr = new TransformationRequest(type);
        type.traverse(new JVisitor() {

            @Override
            public boolean visit(@Nonnull JDefinedClassOrInterface type) {
                if (className(type).contains("BackportBaseTest")) {
                    System.out.println("visit2: " + className(type));
                }
                backport(type.getSuperClass());
                for (JInterface imp : type.getImplements()) {
                    if (className(type).contains("BackportBaseTest")) {
                        System.out.println("imp: " + className(imp));
                    }
                    backport(imp);
                }
                return super.visit(type);
            }

            @Override
            public boolean visit(@Nonnull JField field) {
                backport(field.getType());
                return super.visit(field);
            }

            @Override
            public boolean visit(@Nonnull JVariableRef variableRef) {
                backport(variableRef.getType());
                return super.visit(variableRef);
            }

            @Override
            public boolean visit(@Nonnull JClassLiteral classLiteral) {
                backport(classLiteral.getRefType());
                return super.visit(classLiteral);
            }

            @Override
            public boolean visit(@Nonnull JMethodCall methodCall) {
                backport(methodCall.getReceiverType());
                return super.visit(methodCall);
            }

            @Override
            public boolean visit(@Nonnull JMethod method) {
                backport(method.getType());
                for (JParameter param : method.getParams()) {
                    backport(param.getType());
                }
                return super.visit(method);
            }

            private void backport(JType type) {
                if (checkType(type)) {
                    JClassOrInterface jType = (JClassOrInterface) type;
                    tr.append(new ChangeEnclosingPackage(jType, withJackport(jType.getEnclosingPackage())));
                    System.out.println(Util.className(jType) + " -> " + withJackport(jType.getEnclosingPackage()) + "." + type.getName() + " (" + type.getSourceInfo() + " " + type.getSourceInfo().getFileName() + ")");
                }
            }
        });
        tr.commit();
    }

    private static <T> String str(Iterator<T> itr) {
        return str(itr, new ToString<T>() {
            @Override
            public String toString(T value) {
                return value.toString();
            }
        });
    }

    private static <T> String str(Iterator<T> itr, ToString<T> toString) {
        if (itr == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("[");
        while (itr.hasNext()) {
            T value = itr.next();
            builder.append(value != null ? toString.toString(value) : null);
            if (itr.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private interface ToString<T> {
        String toString(T value);
    }
}
