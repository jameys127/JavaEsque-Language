package com.jamey.compiler.Parser;

public record BinaryExp(Exp l, Op op, Exp r) implements Exp{
    
}
