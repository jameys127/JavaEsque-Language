package com.jamey.compiler.Parser;

import java.util.Optional;

public record IfStmt(Exp e, Stmt stmt, Optional<Stmt> elseStmt) implements Stmt{
    
}
