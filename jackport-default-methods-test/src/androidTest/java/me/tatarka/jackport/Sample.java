package me.tatarka.jackport;

public interface Sample {
    
    String foo(); 
    
    String bar();
    
    abstract class Impl implements Sample {

        @Override
        public String bar() {
            return "bar"; 
        }
    }
}
