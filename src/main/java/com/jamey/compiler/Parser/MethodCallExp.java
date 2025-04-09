package com.jamey.compiler.Parser;

import java.util.List;

public record MethodCallExp(Exp e, String name, List<Exp> exps) implements Exp{
    
}
