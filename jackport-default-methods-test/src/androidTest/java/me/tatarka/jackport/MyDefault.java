package me.tatarka.jackport;

public interface MyDefault {
   
    String foo();

    default String bar() {
        return "bar";
    }
}
