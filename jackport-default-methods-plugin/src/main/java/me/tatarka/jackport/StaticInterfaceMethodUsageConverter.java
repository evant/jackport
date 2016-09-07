package me.tatarka.jackport;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JExpression;
import com.android.jack.ir.ast.JInterface;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.lookup.JLookup;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import me.tatarka.jackport.marker.DefaultMethodInterface;
import me.tatarka.jackport.util.Util;

@Description("Convert calls to static methods on interface to subclass")
@Constraint(need = {JMethodCall.class, DefaultMethodInterface.class})
@Transform(modify = {JMethodCall.class})
public class StaticInterfaceMethodUsageConverter implements RunnableSchedulable<JDefinedClassOrInterface> {

    @Nonnull
    private final JLookup lookup = Jack.getSession().getLookup();

    private class StaticMethodCallConverter extends JVisitor {

        @Nonnull
        private final TransformationRequest tr;

        StaticMethodCallConverter(@Nonnull TransformationRequest tr) {
            this.tr = tr;
        }

        @Override
        public boolean visit(@Nonnull JMethodCall methodCall) {
            if (methodCall.getInstance() != null) {
                return super.visit(methodCall);
            }
            JClassOrInterface receiver = methodCall.getReceiverType();
            if (!(receiver instanceof JInterface)) {
                return super.visit(methodCall);
            }
            JInterface iface = (JInterface) receiver;
            DefaultMethodInterface marker = Util.getMarker(iface, DefaultMethodInterface.class);
            if (marker == null) {
                return super.visit(methodCall);
            }
            JDefinedClass defaultClass = marker.lookup(iface);
            List<JExpression> args = methodCall.getArgs();
            List<JType> argTypes = new ArrayList<>(args.size());
            for (JExpression arg : args) {
                argTypes.add(arg.getType());
            }
            JMethodCall newCall = new JMethodCall(
                    methodCall.getSourceInfo(),
                    null,
                    defaultClass,
                    defaultClass.getOrCreateMethodIdWide(methodCall.getMethodName(), argTypes, MethodKind.STATIC),
                    methodCall.getType(),
                    false
            );

            tr.append(new Replace(methodCall, newCall));

            return super.visit(methodCall);
        }
    }

    @Override
    public void run(@Nonnull JDefinedClassOrInterface type) {
        TransformationRequest request = new TransformationRequest(type);
        StaticMethodCallConverter visitor = new StaticMethodCallConverter(request);
        visitor.accept(type);
        request.commit();
    }
}
