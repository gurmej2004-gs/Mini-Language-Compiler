package com.minilang.compiler.ast;
public interface Visitor<T> {
    T visit(ProgramNode node);
    T visit(AssignNode node);
    T visit(AddExprNode node);
    T visit(SubExprNode node);
    T visit(NumExprNode node);
    T visit(IdExprNode node);
}