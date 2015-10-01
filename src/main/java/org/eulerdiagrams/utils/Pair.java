package org.eulerdiagrams.utils;

public class Pair<T, U> {
    public T car;
    public U cdr;

    public Pair(T car, U cdr) {
        this.car = car;
        this.cdr = cdr;
    }
}