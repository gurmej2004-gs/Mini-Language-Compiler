package com.minilang.compiler.ast;
public class AssignNode extends StatementNode {
    private String id;
    private ExprNode expr;

    public AssignNode(String id, ExprNode expr) {
        this.id = id;
        this.expr = expr;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getId() { return id; }
    public ExprNode getExpr() { return expr; }

    @Override
    public String toString() {
        return id + " = " + expr.toString();
    }
}