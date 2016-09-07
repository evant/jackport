package me.tatarka.jackport;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

// 2
@RunWith(AndroidJUnit4.class)
public class DefaultMethodTest {

    @Test
    public void testDeclaredImpl() {
        MyDefault myDefault = new MyDefaultImpl();

        assertEquals("foo", myDefault.foo());
        assertEquals("bar", myDefault.bar());
    }

    @Test
    public void testAnonymousImpl() {
        MyDefault myDefault = new MyDefault() {
            @Override
            public String foo() {
                return "foo";
            }
        };

        assertEquals("foo", myDefault.foo());
        assertEquals("bar", myDefault.bar());
    }

    @Test
    public void testLambdaImpl() {
        MyDefault myDefault = () -> "foo";

        assertEquals("foo", myDefault.foo());
        assertEquals("bar", myDefault.bar());
    }

    @Test
    public void testMethodRefImpl() {
        MyDefault myDefault = new MyDefaultImpl()::foo;

        assertEquals("foo", myDefault.foo());
        assertEquals("bar", myDefault.bar());
    }

    @Test
    public void testAlreadyExtends() {
        MyDefaultImpl2 myDefault = new MyDefaultImpl2();

        assertEquals("foo", myDefault.foo());
        assertEquals("bar", myDefault.bar());
        assertEquals("baz", myDefault.baz());
    }
}
