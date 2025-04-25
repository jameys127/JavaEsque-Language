package com.jamey.compiler.Parser;

import java.util.Optional;

public record AssignStmt(Optional<ThisExp> thisTarget, String name, Exp e) implements Stmt{
    
}
