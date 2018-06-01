package com.colaorange.commons.util;

/**
 * to help changing value of a final variable that be used in a closure 
 * @author Dennis.Chen
 */
public class Var<T> {
    public T value;

    public Var(){

    }
    public Var(T value){
        this.value = value;
    }
}
