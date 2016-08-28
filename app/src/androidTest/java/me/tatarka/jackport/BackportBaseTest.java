package me.tatarka.jackport;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * Test different ways to access classes that are backported. Note: since a single backport will
 * effect all usages, each one of these tests <em>must</em> use a different class.
 */
@RunWith(AndroidJUnit4.class)
public class BackportBaseTest {

    @Test
    public void method_call() {
        Objects.equals(1, 2);
    }

    @Test
    public void method_return() {
        getSupplier();
    }

    @Test
    public void method_arg() {
        takeBiPredicate(null);
    }

    @Test
    public void class_implements() {
        Class<?> myClass = MyPredicate.class;
    }

    @Test
    public void class_literal() {
        Class<?> myClass = BiFunction.class;
    }

    @Test
    public void class_new_annonamous_instance() {
        final String[] result = new String[1];
        new BiConsumer<String, String>() {
            @Override
            public void accept(String s, String s2) {
                result[0] = s + s2;
            }
        }.accept("1", "2");
        
        assertEquals("12", result[0]);
    }

    @Test
    public void new_lambda() {
        IntSupplier s = () -> 0;
    }

    @Test
    public void static_method_in_interface() {
        IntUnaryOperator ident = IntUnaryOperator.identity();
    }

    @Test
    public void call_default_method_on_instance() {
        DoubleUnaryOperator f = DoubleUnaryOperator.identity().andThen(new DoubleUnaryOperator() {
            @Override
            public double applyAsDouble(double operand) {
                return operand + 1;
            }
        });
        
        assertEquals(2, f.applyAsDouble(1), 0);
    }

    @Test
    public void call_default_method_on_subclass() {
        Function<String, String> f = new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s + "2";
            }
        }.andThen(new Function<String, String>() {
            @Override
            public String apply(String s) {
                return s + "3";
            }
        });
        
        assertEquals("123", f.apply("1"));
    }

    private static Supplier<String> getSupplier() {
        return null;
    }

    private static void takeBiPredicate(BiPredicate<String, String> pr) {
    }

    private static class MyPredicate implements Predicate<String> {
        @Override
        public boolean test(String s) {
            return true;
        }
    }
}
