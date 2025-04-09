package com.jamey.compiler.Parser;

public record BinaryExp(Exp r, Op op, Exp l) implements Exp{
    
}
