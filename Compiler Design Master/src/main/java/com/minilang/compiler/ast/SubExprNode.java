package com.minilang.compiler.ast;

public class SubExprNode extends ExprNode {
   public   final ExprNode left;
    public  final ExprNode right;

    public SubExprNode(ExprNode left, ExprNode right) {
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
        return "(" + left.toString() + " - " + right.toString() + ")";
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return null;
    }
}