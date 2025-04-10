package com.jamey.compiler.Parser;

import java.util.List;

public record MethodCallExp(Exp e, List<MethodCall> methodCalls) implements Exp{
    public record MethodCall(String name, List<Exp> exps){}
}
