package com.jamey.compiler.Parser;

import java.util.List;

public record NewExp(ClassType type, List<Exp> exps) implements Exp{
    
}
