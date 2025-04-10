package com.jamey.compiler.Parser;

import java.util.List;
import java.util.Optional;

public record NewExp(ClassType type, Optional<List<Exp>> exps) implements Exp{
    
}
