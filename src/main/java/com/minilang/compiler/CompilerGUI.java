package com.minilang.compiler;

import com.minilang.compiler.ast.AssignNode;
import com.minilang.compiler.ast.NumExprNode;
import com.minilang.compiler.ast.ProgramNode;
import com.minilang.compiler.ast.StatementNode;
import com.minilang.compiler.generated.MiniLangLexer;
import com.minilang.compiler.generated.MiniLangParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompilerGUI extends JFrame {

    private JTextArea inputTextArea;
    private JTextArea astOutputTextArea;
    private JTextArea assemblyOutputTextArea;
    private JLabel statusLabel;
    private final Compiler compilerInstance; // Hold an instance of your Compiler logic

    public CompilerGUI() {
        super("MiniLang Compiler"); // Set the window title
        compilerInstance = new Compiler(); // Create an instance of your compiler logic

        // --- Frame Setup ---
        setSize(800, 600); // Set initial window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close operation
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout for main layout with gaps

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("MiniLang Code Input"));
        inputTextArea = new JTextArea(10, 40); // 10 rows, 40 columns
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputTextArea.setText("x = 3 + 5;\ny = x - 2;"); // Default input
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.WEST); // Add input panel to the left

        // --- Control Panel ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton compileButton = new JButton("Compile & Generate Assembly");
        compileButton.addActionListener(e -> compileCode()); // Add action listener
        controlPanel.add(compileButton);
        add(controlPanel, BorderLayout.NORTH); // Add control panel to the top

        // --- Output Panel ---
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new GridLayout(2, 1, 10, 10)); // 2 rows, 1 column, with gaps

        JPanel astPanel = new JPanel(new BorderLayout());
        astPanel.setBorder(BorderFactory.createTitledBorder("AST Output"));
        astOutputTextArea = new JTextArea(10, 40);
        astOutputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        astOutputTextArea.setEditable(false); // Make it read-only
        JScrollPane astScrollPane = new JScrollPane(astOutputTextArea);
        astPanel.add(astScrollPane, BorderLayout.CENTER);
        outputPanel.add(astPanel);

        JPanel assemblyPanel = new JPanel(new BorderLayout());
        assemblyPanel.setBorder(BorderFactory.createTitledBorder("Generated Assembly (output.asm)"));
        assemblyOutputTextArea = new JTextArea(10, 40);
        assemblyOutputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        assemblyOutputTextArea.setEditable(false); // Make it read-only
        JScrollPane assemblyScrollPane = new JScrollPane(assemblyOutputTextArea);
        assemblyPanel.add(assemblyScrollPane, BorderLayout.CENTER);
        outputPanel.add(assemblyPanel);

        add(outputPanel, BorderLayout.CENTER); // Add output panel to the center

        // --- Status Bar ---
        statusLabel = new JLabel("Ready.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding
        add(statusLabel, BorderLayout.SOUTH); // Add status label to the bottom

        setVisible(true); // Make the window visible
    }

    private void compileCode() {
        String input = inputTextArea.getText();
        astOutputTextArea.setText(""); // Clear previous output
        assemblyOutputTextArea.setText("");
        statusLabel.setText("Compiling...");

        // Redirect System.out to capture AST output
        ByteArrayOutputStream astStream = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(astStream));

        try {
            CharStream charStream = CharStreams.fromString(input);
            MiniLangLexer lexer = new MiniLangLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // --- CORRECTED ORDER: Initialize parser BEFORE using it for listeners ---
            MiniLangParser parser = new MiniLangParser(tokens);

            // Custom error listener to capture syntax errors
            ErrorListener errorListener = new ErrorListener();
            lexer.removeErrorListeners(); // Remove default console listener
            parser.removeErrorListeners(); // Remove default console listener
            lexer.addErrorListener(errorListener);
            parser.addErrorListener(errorListener);
            // --- END CORRECTED ORDER ---

            MiniLangParser.ProgramContext tree = parser.program();

            if (errorListener.hasErrors()) {
                statusLabel.setText("Compilation failed: Syntax Error!");
                astOutputTextArea.setText("Syntax Error:\n" + errorListener.getErrors());
                System.setOut(oldOut); // Restore System.out before returning
                return; // Stop if there are syntax errors
            }

            Compiler.ASTBuilder astBuilder = new Compiler.ASTBuilder(); // Use the nested ASTBuilder
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
                    // You might want to add more specific AST printing logic here for other expr types
                }
            }

            // Restore System.out
            System.out.flush();
            System.setOut(oldOut);
            astOutputTextArea.setText(astStream.toString());

            // Correctly generate assembly to StringWriter and then to file
            StringWriter assemblyStringWriter = new StringWriter();
            compilerInstance.generateAssembly(ast, assemblyStringWriter); // Generate to StringWriter

            // Now write the content of the StringWriter to the actual file
            try (FileWriter fileWriter = new FileWriter("output.asm")) {
                fileWriter.write(assemblyStringWriter.toString());
            } catch (IOException ex) {
                statusLabel.setText("Error writing assembly file: " + ex.getMessage());
                ex.printStackTrace();
                return;
            }

            assemblyOutputTextArea.setText(assemblyStringWriter.toString()); // Display in GUI from StringWriter

            statusLabel.setText("Compilation successful! Assembly generated to output.asm");

        } catch (Exception ex) {
            System.setOut(oldOut); // Restore System.out in case of other exceptions
            statusLabel.setText("Compilation failed: " + ex.getMessage());
            astOutputTextArea.setText("Error during compilation:\n" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Custom Error Listener to capture ANTLR errors
    private static class ErrorListener extends BaseErrorListener {
        private final StringBuilder errors = new StringBuilder();
        private boolean hasErrors = false;

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            hasErrors = true;
            errors.append(String.format("line %d:%d %s\n", line, charPositionInLine, msg));
        }

        public boolean hasErrors() {
            return hasErrors;
        }

        public String getErrors() {
            return errors.toString();
        }
    }

    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(CompilerGUI::new);
    }
}
