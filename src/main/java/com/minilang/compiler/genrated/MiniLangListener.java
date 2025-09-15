// Generated from MiniLang.g4 by ANTLR 4.13.1
package com.minilang.compiler.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MiniLangParser}.
 */
public interface MiniLangListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MiniLangParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(MiniLangParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniLangParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(MiniLangParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniLangParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(MiniLangParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniLangParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(MiniLangParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniLangParser#assignStmt}.
	 * @param ctx the parse tree
	 */
	void enterAssignStmt(MiniLangParser.AssignStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniLangParser#assignStmt}.
	 * @param ctx the parse tree
	 */
	void exitAssignStmt(MiniLangParser.AssignStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MiniLangParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void enterExprStmt(MiniLangParser.ExprStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link MiniLangParser#exprStmt}.
	 * @param ctx the parse tree
	 */
	void exitExprStmt(MiniLangParser.ExprStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IdExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIdExpr(MiniLangParser.IdExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IdExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIdExpr(MiniLangParser.IdExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NumExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterNumExpr(MiniLangParser.NumExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NumExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitNumExpr(MiniLangParser.NumExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code SubExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterSubExpr(MiniLangParser.SubExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code SubExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitSubExpr(MiniLangParser.SubExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddExpr(MiniLangParser.AddExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddExpr}
	 * labeled alternative in {@link MiniLangParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddExpr(MiniLangParser.AddExprContext ctx);
}