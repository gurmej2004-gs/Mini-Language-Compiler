package com.minilang.compiler;

import com.minilang.compiler.ast.*;
import com.minilang.compiler.generated.MiniLangBaseVisitor;
import com.minilang.compiler.generated.MiniLangLexer;
import com.minilang.compiler.generated.MiniLangParser;
import org.antlr.v4.runtime.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
    public static void main(String[] args) throws Exception {
        String input = "x = 3 + 5; y = x - 2;";
        CharStream charStream = CharStreams.fromString(input);
        MiniLangLexer lexer = new MiniLangLexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniLangParser parser = new MiniLangParser(tokens);
        MiniLangParser.ProgramContext tree = parser.program();

        ASTBuilder astBuilder = new ASTBuilder();
        ProgramNode ast = (ProgramNode) astBuilder.visit(tree);

        System.out.println("AST built: " + ast.getClass().getSimpleName());
        for (StatementNode stmt : ast.getStatements()) {
            System.out.println("Statement: " + stmt.getClass().getSimpleName());
            if (stmt instanceof AssignNode) {
                System.out.println("  ID: " + ((AssignNode) stmt).getId());
                System.out.println("  Expr: " + ((AssignNode) stmt).getExpr().getClass().getSimpleName());
                if (((AssignNode) stmt).getExpr() instanceof NumExprNode) {
                    System.out.println("  Value: " + ((NumExprNode) ((AssignNode) stmt).getExpr()).getValue());
                }
            }
        }

        generateAssembly(ast, "output.asm");
    }

    public static void generateAssembly(ProgramNode ast, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("section .data\n");
            writer.write("  x_msg db 'x =', 0\n");
            writer.write("  x_msg_len equ $ - x_msg\n");
            writer.write("  y_msg db 'y =', 0\n");
            writer.write("  y_msg_len equ $ - y_msg\n");
            writer.write("  newline db 0xA, 0\n");
            writer.write("  newline_len equ $ - newline\n");
            writer.write("  num_buf times 20 db 0\n");
            writer.write("section .text\n");
            writer.write("  global _main\n");
            writer.write("_main:\n");
            writer.write("  push rbp\n");
            writer.write("  mov rbp, rsp\n");
            writer.write("  sub rsp, 16\n");

            for (StatementNode stmt : ast.getStatements()) {
                if (stmt instanceof AssignNode) {
                    String id = ((AssignNode) stmt).getId();
                    ExprNode expr = ((AssignNode) stmt).getExpr();
                    int offset = (id.equals("x") ? 8 : 0);
                    if (expr instanceof NumExprNode) {
                        writer.write("  mov rax, " + ((NumExprNode) expr).getValue() + "\n");
                        writer.write("  mov [rbp - " + offset + "], rax\n");
                    } else if (expr instanceof AddExprNode) {
                        AddExprNode add = (AddExprNode) expr;
                        generateExpr(add.getLeft(), writer, id);
                        writer.write("  push rax\n");
                        generateExpr(add.getRight(), writer, id);
                        writer.write("  pop rbx\n");
                        writer.write("  add rax, rbx\n");
                        writer.write("  mov [rbp - " + offset + "], rax\n");
                    } else if (expr instanceof SubExprNode) {
                        SubExprNode sub = (SubExprNode) expr;
                        generateExpr(sub.getLeft(), writer, id);
                        writer.write("  push rax\n");
                        generateExpr(sub.getRight(), writer, id);
                        writer.write("  pop rbx\n");
                        writer.write("  sub rbx, rax\n");
                        writer.write("  mov rax, rbx\n");
                        writer.write("  mov [rbp - " + offset + "], rax\n");
                    }
                }
            }

            writer.write("  mov rax, [rbp - 8]\n");
            writer.write("  lea rsi, [rel num_buf]\n");
            writer.write("  call int_to_string\n");
            writer.write("  mov rax, 0x2000004\n");
            writer.write("  mov rdi, 1\n");
            writer.write("  lea rsi, [rel x_msg]\n");
            writer.write("  mov rdx, x_msg_len\n");
            writer.write("  syscall\n");
            writer.write("  lea rsi, [rel num_buf]\n");
            writer.write("  mov rdx, rcx\n");
            writer.write("  syscall\n");
            writer.write("  lea rsi, [rel newline]\n");
            writer.write("  mov rdx, newline_len\n");
            writer.write("  syscall\n");

            writer.write("  mov rax, [rbp]\n");
            writer.write("  lea rsi, [rel num_buf]\n");
            writer.write("  call int_to_string\n");
            writer.write("  mov rax, 0x2000004\n");
            writer.write("  mov rdi, 1\n");
            writer.write("  lea rsi, [rel y_msg]\n");
            writer.write("  mov rdx, y_msg_len\n");
            writer.write("  syscall\n");
            writer.write("  lea rsi, [rel num_buf]\n");
            writer.write("  mov rdx, rcx\n");
            writer.write("  syscall\n");
            writer.write("  lea rsi, [rel newline]\n");
            writer.write("  mov rdx, newline_len\n");
            writer.write("  syscall\n");

            writer.write("  mov rax, 0x2000001\n");
            writer.write("  xor rdi, rdi\n");
            writer.write("  syscall\n");
            writer.write("  leave\n");
            writer.write("  ret\n");

            writer.write("int_to_string:\n");
            writer.write("  push rbx\n"); // Preserve rbx
            writer.write("  push rcx\n"); // Preserve rcx for length
            writer.write("  push rax\n"); // Preserve original value
            writer.write("  mov rbx, 10\n"); // Divisor
            writer.write("  xor rcx, rcx\n"); // Initialize length counter
            writer.write("  mov rdi, rsi\n"); // Start at rsi (num_buf)
            writer.write("  push rdi\n"); // Save start address for null termination
            writer.write("  test rax, rax\n");
            writer.write("  jns .non_negative\n");
            writer.write("  neg rax\n");
            writer.write(".non_negative:\n");
            writer.write("  or rax, rax\n");
            writer.write("  jz .handle_zero\n");
            writer.write(".convert_loop:\n");
            writer.write("  xor rdx, rdx\n");
            writer.write("  div rbx\n");
            writer.write("  add dl, '0'\n");
            writer.write("  mov [rdi], dl\n");
            writer.write("  inc rdi\n");
            writer.write("  inc rcx\n"); // Increment length
            writer.write("  test rax, rax\n");
            writer.write("  jnz .convert_loop\n");
            writer.write("  jmp .done\n");
            writer.write(".handle_zero:\n");
            writer.write("  mov byte [rdi], '0'\n");
            writer.write("  inc rdi\n");
            writer.write("  mov rcx, 1\n"); // Set length to 1 for zero
            writer.write(".done:\n");
            writer.write("  mov byte [rdi], 0\n"); // Null terminate
            writer.write("  pop rdi\n"); // Restore start address
            writer.write("  pop rax\n"); // Restore rax
            writer.write("  pop rcx\n"); // Restore rcx (length)
            writer.write("  pop rbx\n"); // Restore rbx
            writer.write("  ret\n");
        }
    }

    private static void generateExpr(ExprNode expr, FileWriter writer, String id) throws IOException {
        if (expr instanceof NumExprNode) {
            writer.write("  mov rax, " + ((NumExprNode) expr).getValue() + "\n");
        } else if (expr instanceof IdExprNode) {
            int offset = (id.equals("x") ? 8 : 0);
            writer.write("  mov rax, [rbp - " + offset + "]\n");
        }
    }

    public static class ASTBuilder extends MiniLangBaseVisitor<Node> {
        private final Map<String, Integer> symbolTable = new HashMap<>();

        @Override
        public Node visitProgram(MiniLangParser.ProgramContext ctx) {
            List<StatementNode> statements = new ArrayList<>();
            for (var child : ctx.children) {
                if (child instanceof MiniLangParser.StatementContext) {
                    statements.add((StatementNode) visit(child));
                }
            }
            return new ProgramNode(statements);
        }

        @Override
        public Node visitAssignStmt(MiniLangParser.AssignStmtContext ctx) {
            String id = ctx.ID().getText();
            ExprNode expr = (ExprNode) visit(ctx.expr());
            if (expr instanceof NumExprNode) {
                symbolTable.put(id, ((NumExprNode) expr).getValue());
            }
            return new AssignNode(id, expr);
        }

        @Override
        public Node visitAddExpr(MiniLangParser.AddExprContext ctx) {
            ExprNode left = (ExprNode) visit(ctx.expr(0));
            ExprNode right = (ExprNode) visit(ctx.expr(1));
            if (left instanceof NumExprNode && right instanceof NumExprNode) {
                return new NumExprNode(((NumExprNode) left).getValue() + ((NumExprNode) right).getValue());
            } else if (left instanceof NumExprNode && right instanceof IdExprNode) {
                Integer rightValue = symbolTable.get(((IdExprNode) right).getId());
                if (rightValue != null) {
                    return new NumExprNode(((NumExprNode) left).getValue() + rightValue);
                }
            } else if (left instanceof IdExprNode && right instanceof NumExprNode) {
                Integer leftValue = symbolTable.get(((IdExprNode) left).getId());
                if (leftValue != null) {
                    return new NumExprNode(leftValue + ((NumExprNode) right).getValue());
                }
            }
            return new AddExprNode(left, right);
        }

        @Override
        public Node visitSubExpr(MiniLangParser.SubExprContext ctx) {
            ExprNode left = (ExprNode) visit(ctx.expr(0));
            ExprNode right = (ExprNode) visit(ctx.expr(1));
            if (left instanceof NumExprNode && right instanceof NumExprNode) {
                return new NumExprNode(((NumExprNode) left).getValue() - ((NumExprNode) right).getValue());
            } else if (left instanceof NumExprNode && right instanceof IdExprNode) {
                Integer rightValue = symbolTable.get(((IdExprNode) right).getId());
                if (rightValue != null) {
                    return new NumExprNode(((NumExprNode) left).getValue() - rightValue);
                }
            } else if (left instanceof IdExprNode && right instanceof NumExprNode) {
                Integer leftValue = symbolTable.get(((IdExprNode) left).getId());
                if (leftValue != null) {
                    return new NumExprNode(leftValue - ((NumExprNode) right).getValue());
                }
            }
            return new SubExprNode(left, right);
        }

        @Override
        public Node visitNumExpr(MiniLangParser.NumExprContext ctx) {
            return new NumExprNode(Integer.parseInt(ctx.NUMBER().getText()));
        }

        @Override
        public Node visitIdExpr(MiniLangParser.IdExprContext ctx) {
            return new IdExprNode(ctx.ID().getText());
        }
    }
}