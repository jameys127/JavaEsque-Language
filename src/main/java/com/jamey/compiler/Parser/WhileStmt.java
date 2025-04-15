package com.jamey.compiler.Parser;

import java.util.List;

public record WhileStmt(Exp e, List<Stmt> stmt) implements Stmt{
    
}
