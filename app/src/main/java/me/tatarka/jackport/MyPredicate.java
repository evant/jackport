package me.tatarka.jackport;

import java.util.function.Predicate;

class MyPredicate implements Predicate<String> {
    @Override
    public boolean test(String s) {
        return true;
    }
}
