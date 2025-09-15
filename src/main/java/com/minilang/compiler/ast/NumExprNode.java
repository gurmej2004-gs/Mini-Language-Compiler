package com.minilang.compiler.ast;
public class NumExprNode extends ExprNode {
    private final int value;

    public NumExprNode(int value) {
        this.value = value;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int getValue() { return value; }
}
