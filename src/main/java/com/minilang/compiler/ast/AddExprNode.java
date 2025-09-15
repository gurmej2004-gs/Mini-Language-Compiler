package com.minilang.compiler.ast;

public class AddExprNode extends ExprNode {
    private final ExprNode left;
    private final ExprNode right;

    public AddExprNode(ExprNode left, ExprNode right) {
        this.left = left;
        this.right = right;
    }

    public ExprNode getLeft() {
        return left;
    }

    public ExprNode getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + left.toString() + " + " + right.toString() + ")";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}