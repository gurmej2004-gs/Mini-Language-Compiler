// Generated from MiniLang.g4 by ANTLR 4.13.1
package com.minilang.compiler.generated;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MiniLangParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MiniLangVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MiniLangParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(MiniLangParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniLangParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(MiniLangParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniLangParser#assignStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignStmt(MiniLangParser.AssignStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link MiniLangParser#exprStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprStmt(MiniLangParser.ExprStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IdExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdExpr(MiniLangParser.IdExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumExpr(MiniLangParser.NumExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code SubExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubExpr(MiniLangParser.SubExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AddExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddExpr(MiniLangParser.AddExprContext ctx);
}