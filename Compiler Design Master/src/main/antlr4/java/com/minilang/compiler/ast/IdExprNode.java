package com.minilang.compiler.ast;

public class IdExprNode extends ExprNode {
    private final String id;

    public IdExprNode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}