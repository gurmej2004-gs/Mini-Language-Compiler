package com.minilang.compiler;

import com.minilang.compiler.ast.*;
import com.minilang.compiler.generated.MiniLangBaseVisitor;
import com.minilang.compiler.generated.MiniLangLexer;
import com.minilang.compiler.generated.MiniLangParser;
import org.antlr.v4.runtime.*;
import java.io.Writer; // Import Writer instead of FileWriter
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections; // For Collections.reverse

public class Compiler {
    // No main method here anymore, it's in CompilerGUI

    // Symbol table to map variable names to their stack offsets (relative to RBP)
    // Positive values mean higher address (e.g., function args), negative mean lower (e.g., local vars)
    private Map<String, Integer> variableOffsets;
    private int currentStackOffset; // Tracks the next available offset for a new variable
    private List<String> declaredVariablesOrder; // To keep track of variable declaration order for printing

    public Compiler() {
        // Initialize state for each compilation run
        this.variableOffsets = new HashMap<>();
        this.currentStackOffset = 0; // Starting from 0 for local variables (rbp-0, rbp-8, etc.)
        this.declaredVariablesOrder = new ArrayList<>();
    }


    // Modified generateAssembly to write to any Writer, not just a file
    public void generateAssembly(ProgramNode ast, Writer writer) throws IOException {
        // Reset state for a new compilation
        this.variableOffsets = new HashMap<>();
        this.currentStackOffset = 0;
        this.declaredVariablesOrder = new ArrayList<>();

        // Generate data section
        writer.write("section .data\n");
        writer.write("  newline db 0xA, 0\n");
        writer.write("  newline_len equ $ - newline\n");
        writer.write("  num_buf times 20 db 0\n"); // Buffer for integer to string conversion

        // Generate text section header
        writer.write("section .text\n");
        writer.write("  global _main\n"); // Entry point for macOS
        // On Linux, often `_start` or `main` depending on linker setup.
        // For simplicity, we'll stick with _main which is common on macOS and some Linux setups with C runtime.

        writer.write("_main:\n");
        writer.write("  push rbp\n");
        writer.write("  mov rbp, rsp\n");
        // We'll calculate stack allocation dynamically based on variables found
        // For now, let's just reserve some minimal space, we'll adjust this if needed
        // Sub rsp, 16 is still here from previous; will be more dynamic if we track total variable size.

        // --- Generate Code for Statements ---
        for (StatementNode stmt : ast.getStatements()) {
            if (stmt instanceof AssignNode) {
                String id = ((AssignNode) stmt).getId();
                ExprNode expr = ((AssignNode) stmt).getExpr();

                // Allocate stack space for the variable if it's new
                if (!variableOffsets.containsKey(id)) {
                    // Allocate 8 bytes for a 64-bit integer
                    currentStackOffset -= 8;
                    variableOffsets.put(id, currentStackOffset);
                    declaredVariablesOrder.add(id); // Keep track of declaration order
                }

                int offset = variableOffsets.get(id);

                if (expr instanceof NumExprNode) {
                    writer.write("  ; Assign " + id + " = " + ((NumExprNode) expr).getValue() + "\n");
                    writer.write("  mov rax, " + ((NumExprNode) expr).getValue() + "\n");
                    writer.write("  mov [rbp " + offset + "], rax\n");
                } else if (expr instanceof AddExprNode) {
                    writer.write("  ; Assign " + id + " = " + ((AddExprNode) expr).getLeft().getClass().getSimpleName() + " + " + ((AddExprNode) expr).getRight().getClass().getSimpleName() + "\n");
                    AddExprNode add = (AddExprNode) expr;
                    generateExpr(add.getLeft(), writer, id); // Evaluate left operand into rax
                    writer.write("  push rax\n");           // Push rax onto stack
                    generateExpr(add.getRight(), writer, id); // Evaluate right operand into rax
                    writer.write("  pop rbx\n");             // Pop left operand into rbx
                    writer.write("  add rax, rbx\n");        // rax = rax + rbx (right + left)
                    writer.write("  mov [rbp " + offset + "], rax\n");
                } else if (expr instanceof SubExprNode) {
                    writer.write("  ; Assign " + id + " = " + ((SubExprNode) expr).getLeft().getClass().getSimpleName() + " - " + ((SubExprNode) expr).getRight().getClass().getSimpleName() + "\n");
                    SubExprNode sub = (SubExprNode) expr;
                    generateExpr(sub.getLeft(), writer, id); // Evaluate left operand into rax
                    writer.write("  push rax\n");           // Push rax onto stack
                    generateExpr(sub.getRight(), writer, id); // Evaluate right operand into rax
                    writer.write("  pop rbx\n");             // Pop left operand into rbx
                    writer.write("  sub rbx, rax\n");        // rbx = rbx - rax (left - right)
                    writer.write("  mov rax, rbx\n");        // Move result to rax
                    writer.write("  mov [rbp " + offset + "], rax\n");
                } else if (expr instanceof IdExprNode) {
                    writer.write("  ; Assign " + id + " = " + ((IdExprNode) expr).getId() + "\n");
                    IdExprNode idExpr = (IdExprNode) expr;
                    int sourceOffset = variableOffsets.get(idExpr.getId()); // Get offset of source variable
                    writer.write("  mov rax, [rbp " + sourceOffset + "]\n"); // Load source variable value
                    writer.write("  mov [rbp " + offset + "], rax\n");       // Store in target variable
                }
            }
        }

        // Adjust stack pointer after all allocations
        // For simplicity, we just use the final currentStackOffset.
        // A more robust approach would pre-calculate total needed stack space.
        if (currentStackOffset < 0) {
            writer.write("  sub rsp, " + Math.abs(currentStackOffset) + "\n");
        }


        // --- Generate Code for Printing All Declared Variables ---
        // Dynamically add db messages for each variable
        for (String varName : declaredVariablesOrder) {
            writer.write("  " + varName + "_msg db '" + varName + " =', 0\n");
            writer.write("  " + varName + "_msg_len equ $ - " + varName + "_msg\n");
        }

        writer.write("\n"); // Add a newline for readability in assembly

        for (String varName : declaredVariablesOrder) {
            int offset = variableOffsets.get(varName);

            writer.write("  ; Print " + varName + " = value\n");
            writer.write("  mov rax, [rbp " + offset + "]\n"); // Load variable value
            writer.write("  lea rsi, [rel num_buf]\n");
            writer.write("  call int_to_string\n"); // Convert integer to string in num_buf

            // Print variable name message
            writer.write("  mov rax, 0x2000004\n"); // syscall for write (macOS)
            writer.write("  mov rdi, 1\n");         // stdout file descriptor
            writer.write("  lea rsi, [rel " + varName + "_msg]\n"); // Address of variable name message
            writer.write("  mov rdx, " + varName + "_msg_len\n"); // Length of message
            writer.write("  syscall\n");

            // Print converted number string
            writer.write("  mov rax, 0x2000004\n");
            writer.write("  mov rdi, 1\n");
            writer.write("  lea rsi, [rel num_buf]\n"); // Address of converted number string
            writer.write("  mov rdx, rcx\n");           // rcx holds length from int_to_string
            writer.write("  syscall\n");

            // Print newline
            writer.write("  mov rax, 0x2000004\n");
            writer.write("  mov rdi, 1\n");
            writer.write("  lea rsi, [rel newline]\n");
            writer.write("  mov rdx, newline_len\n");
            writer.write("  syscall\n");
            writer.write("\n"); // Add a newline for readability
        }

        // --- Program Exit ---
        writer.write("  mov rax, 0x2000001\n"); // syscall for exit (macOS)
        writer.write("  xor rdi, rdi\n");       // exit code 0
        writer.write("  syscall\n");
        writer.write("  leave\n");              // Restore rbp and rsp
        writer.write("  ret\n");                // Return from _main

        // --- Helper Function: int_to_string (Converts RAX to ASCII string in num_buf) ---
        // This function places the string in reverse order in the buffer
        // and returns the length in rcx.
        writer.write("int_to_string:\n");
        writer.write("  push rbx\n"); // Preserve rbx
        writer.write("  push rcx\n"); // Preserve rcx for length
        writer.write("  push rdi\n"); // Preserve rdi (original buffer start)
        writer.write("  push rsi\n"); // Preserve rsi (buffer address from caller)
        writer.write("  mov rbp, rsi\n"); // rbp now points to the end of the buffer (for writing backwards)

        writer.write("  mov rbx, 10\n"); // Divisor
        writer.write("  xor rcx, rcx\n"); // Initialize length counter
        writer.write("  mov byte [rbp], 0\n"); // Null terminate at the current end of buffer
        writer.write("  dec rbp\n"); // Move pointer to start writing digits from right to left

        writer.write("  cmp rax, 0\n");
        writer.write("  jge .non_negative_int_to_string\n");
        writer.write("  neg rax\n"); // Make number positive for conversion
        writer.write("  mov byte [rbp], '-'\n"); // Place '-' sign
        writer.write("  dec rbp\n");
        writer.write("  inc rcx\n"); // Count the '-' character
        writer.write(".non_negative_int_to_string:\n");

        writer.write("  cmp rax, 0\n");
        writer.write("  jz .handle_zero_int_to_string\n");

        writer.write(".convert_loop_int_to_string:\n");
        writer.write("  xor rdx, rdx\n"); // Clear rdx for division
        writer.write("  div rbx\n");    // rax = rax / 10, rdx = rax % 10
        writer.write("  add dl, '0'\n"); // Convert remainder to ASCII digit
        writer.write("  mov [rbp], dl\n"); // Store digit
        writer.write("  dec rbp\n");    // Move pointer for next digit
        writer.write("  inc rcx\n");    // Increment length
        writer.write("  cmp rax, 0\n");
        writer.write("  jnz .convert_loop_int_to_string\n");
        writer.write("  jmp .done_int_to_string\n");

        writer.write(".handle_zero_int_to_string:\n");
        writer.write("  mov byte [rbp], '0'\n");
        writer.write("  dec rbp\n");
        writer.write("  mov rcx, 1\n"); // Length is 1 for "0"
        writer.write(".done_int_to_string:\n");

        // rbp now points to the start of the string (or just before it if string is empty)
        // rsi needs to point to the actual start of the string in the buffer
        writer.write("  inc rbp\n"); // Move rbp to point to the first character
        writer.write("  mov rsi, rbp\n"); // Set rsi to the start of the string

        writer.write("  pop rsi\n"); // Restore original rsi
        writer.write("  pop rdi\n"); // Restore original rdi
        writer.write("  pop rax\n"); // Restore original rax
        writer.write("  pop rcx\n"); // Restore original rcx (length)
        writer.write("  pop rbx\n"); // Restore original rbx
        writer.write("  ret\n");
    }

    // This method now accepts a Writer and uses the variableOffsets map
    private void generateExpr(ExprNode expr, Writer writer, String contextId) throws IOException {
        if (expr instanceof NumExprNode) {
            writer.write("  mov rax, " + ((NumExprNode) expr).getValue() + "\n");
        } else if (expr instanceof IdExprNode) {
            String id = ((IdExprNode) expr).getId();
            if (!variableOffsets.containsKey(id)) {
                // This indicates an undeclared variable being used
                // For a real compiler, this would be a semantic error.
                // For now, let's assume it maps to an uninitialized default location or throw an error.
                // For simplicity here, we'll just use a default offset but flag it as a potential issue.
                // In a robust compiler, this would be caught in a semantic analysis phase.
                writer.write("  ; WARNING: Using undeclared variable " + id + "\n");
                writer.write("  mov rax, 0 ; Defaulting to 0\n"); // Or throw a RuntimeException
            } else {
                int offset = variableOffsets.get(id);
                writer.write("  mov rax, [rbp " + offset + "]\n"); // Load value from stack
            }
        }
    }

    // ASTBuilder remains public static as it's a nested class used by CompilerGUI
    public static class ASTBuilder extends MiniLangBaseVisitor<Node> {
        // symbolTable here is for constant folding and type checking during AST building
        // It's conceptually separate from the variableOffsets for assembly generation
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
            // Attempt constant folding if possible
            if (expr instanceof NumExprNode) {
                symbolTable.put(id, ((NumExprNode) expr).getValue());
            } else if (expr instanceof IdExprNode) {
                Integer value = symbolTable.get(((IdExprNode) expr).getId());
                if (value != null) {
                    symbolTable.put(id, value);
                    return new AssignNode(id, new NumExprNode(value));
                }
            } else if (expr instanceof AddExprNode) {
                ExprNode left = ((AddExprNode) expr).getLeft();
                ExprNode right = ((AddExprNode) expr).getRight();
                Integer leftVal = (left instanceof NumExprNode) ? ((NumExprNode) left).getValue() : null;
                if (left instanceof IdExprNode) leftVal = symbolTable.get(((IdExprNode) left).getId());
                Integer rightVal = (right instanceof NumExprNode) ? ((NumExprNode) right).getValue() : null;
                if (right instanceof IdExprNode) rightVal = symbolTable.get(((IdExprNode) right).getId());

                if (leftVal != null && rightVal != null) {
                    symbolTable.put(id, leftVal + rightVal);
                    return new AssignNode(id, new NumExprNode(leftVal + rightVal));
                }
            } else if (expr instanceof SubExprNode) {
                ExprNode left = ((SubExprNode) expr).getLeft();
                ExprNode right = ((SubExprNode) expr).getRight();
                Integer leftVal = (left instanceof NumExprNode) ? ((NumExprNode) left).getValue() : null;
                if (left instanceof IdExprNode) leftVal = symbolTable.get(((IdExprNode) left).getId());
                Integer rightVal = (right instanceof NumExprNode) ? ((NumExprNode) right).getValue() : null;
                if (right instanceof IdExprNode) rightVal = symbolTable.get(((IdExprNode) right).getId());

                if (leftVal != null && rightVal != null) {
                    symbolTable.put(id, leftVal - rightVal);
                    return new AssignNode(id, new NumExprNode(leftVal - rightVal));
                }
            }
            return new AssignNode(id, expr);
        }

        @Override
        public Node visitAddExpr(MiniLangParser.AddExprContext ctx) {
            ExprNode left = (ExprNode) visit(ctx.expr(0));
            ExprNode right = (ExprNode) visit(ctx.expr(1));
            // Constant folding (more robustly handled in AssignStmt now, but kept here for general expr)
            if (left instanceof NumExprNode && right instanceof NumExprNode) {
                return new NumExprNode(((NumExprNode) left).getValue() + ((NumExprNode) right).getValue());
            }
            return new AddExprNode(left, right);
        }

        @Override
        public Node visitSubExpr(MiniLangParser.SubExprContext ctx) {
            ExprNode left = (ExprNode) visit(ctx.expr(0));
            ExprNode right = (ExprNode) visit(ctx.expr(1));
            // Constant folding (more robustly handled in AssignStmt now, but kept here for general expr)
            if (left instanceof NumExprNode && right instanceof NumExprNode) {
                return new NumExprNode(((NumExprNode) left).getValue() - ((NumExprNode) right).getValue());
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
