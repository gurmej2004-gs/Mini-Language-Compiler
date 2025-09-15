package com.minilang.compiler.ast;
public abstract class Node {
    public abstract <T> T accept(Visitor<T> visitor);
}