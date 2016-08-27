package me.tatarka.jackport;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
        new BiConsumer<String, String>() {
            @Override
            public void accept(String s, String s2) {

            }
        };
    }

    @Test
    public void new_lambda() {
        Function<String, String> f = s -> s + "test";
    }

    @Test
    public void static_method_in_interface() {
        IntUnaryOperator ident = IntUnaryOperator.identity();
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
