package me.tatarka.jackport;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

@RunWith(AndroidJUnit4.class)
public class BackportOptionalTest {

    @Test
    public void optional() {
        Optional<String> empty = Optional.empty();
        Optional<String> of = Optional.of("1");
    }

    @Test
    public void optional_int() {
        OptionalInt empty = OptionalInt.empty();
        OptionalInt of = OptionalInt.of(1);
    }

    @Test
    public void optional_long() {
        OptionalLong empty = OptionalLong.empty();
        OptionalLong of = OptionalLong.of(1L);
    }

    @Test
    public void optional_double() {
        OptionalDouble empty = OptionalDouble.empty();
        OptionalDouble of = OptionalDouble.of(1.0);
    }
}
