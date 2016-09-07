package me.tatarka.jackport;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

// 1
@RunWith(AndroidJUnit4.class)
public class StaticMethodTest {

    @Test
    public void testStaticMethodCall() {
        assertEquals("foo", MyStatic.foo());
    }
}
