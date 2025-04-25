package com.jamey.compiler.Parser;

import java.util.Optional;

public record ThisExp(Optional<String> parentVar) implements Exp{
    
}
