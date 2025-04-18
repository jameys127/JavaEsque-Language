package com.jamey.compiler.Typechecker;

public class TypecheckerErrorException extends Exception{
    public TypecheckerErrorException(final String message){
        super(message);
    }
}
