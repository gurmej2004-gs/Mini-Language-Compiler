# Mini Language Compiler

A lightweight compiler project developed to explore the fundamental concepts of **compiler design and code generation**. The project takes source code written in a simple custom language and processes it through the compiler pipeline to generate low-level output.

## Overview

This project was developed as part of learning and implementing core concepts of **Compiler Design**.

The compiler demonstrates how source code can be processed and transformed into intermediate and low-level representations, providing practical understanding of how programming languages are translated for machine execution.

## Features

* Custom mini-language syntax
* Source code processing
* Lexical and syntax analysis
* Compiler pipeline implementation
* Code generation
* Assembly (`.asm`) output
* Object (`.o`) output
* Java and Maven-based project structure

## Technologies Used

* **Java**
* **Maven**
* **Compiler Design Concepts**
* **Assembly Language**

## Project Structure

```text
Mini-Language-Compiler/
│
├── src/
│   └── main/
│       └── ...
│
├── Compiler Design Master/
│
├── output/
│
├── output.asm
├── output.o
├── pom.xml
└── README.md
```

## How It Works

The compiler follows a basic translation process:

```text
Source Code
    ↓
Lexical Analysis
    ↓
Syntax Analysis
    ↓
Intermediate Processing
    ↓
Code Generation
    ↓
Assembly Output
    ↓
Object File
```

The generated assembly and object files can be inspected to understand how the original source program is translated into lower-level instructions.

## Getting Started

### Prerequisites

Make sure you have:

* Java JDK installed
* Maven installed
* Git installed

### Clone the Repository

```bash
git clone https://github.com/gurmej2004-gs/Mini-Language-Compiler.git
cd Mini-Language-Compiler
```

### Build the Project

```bash
mvn clean package
```

### Run

Run the compiler using the project's main class or Maven configuration provided in the source code.

## Learning Objectives

This project helped in understanding:

* Compiler architecture
* Lexical and syntax processing
* Source-code translation
* Code generation
* Assembly-level representation
* Java project organization with Maven

## Future Improvements

Possible improvements include:

* Better syntax and semantic error handling
* More language features
* Improved intermediate representation
* Additional optimization techniques
* Support for more data types and control structures
* Improved documentation and test coverage

## Author

**Gurmej Singh**

B.Tech Computer Science & Engineering

[GitHub](https://github.com/gurmej2004-gs)
