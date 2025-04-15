package com.jamey.compiler.Parser;

import java.util.Optional;

public record ReturnStmt(Optional<Exp> e) implements Stmt{
    
}
