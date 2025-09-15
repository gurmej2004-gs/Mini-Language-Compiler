grammar MiniLang;

program: statement+ EOF;
statement: assignStmt | exprStmt;
assignStmt: ID '=' expr ';';
exprStmt: expr ';';
expr: expr '+' expr  # AddExpr
    | expr '-' expr  # SubExpr
    | NUMBER         # NumExpr
    | ID             # IdExpr
    ;

ID: [a-zA-Z][a-zA-Z0-9]*;
NUMBER: [0-9]+;
WS: [ \t\n\r]+ -> skip;
