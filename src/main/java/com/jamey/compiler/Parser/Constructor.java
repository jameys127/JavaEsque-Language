package com.jamey.compiler.Parser;

import java.util.List;
import java.util.Optional;

public class Constructor {
    public final List<VardecStmt> vardecs;
    public final Optional<List<Exp>> exps;
    public final List<Stmt> stmts;

    public Constructor(final List<VardecStmt> vardecs,
                       final Optional<List<Exp>> exps,
                       final List<Stmt> stmts){
        this.vardecs = vardecs;
        this.exps = exps;
        this.stmts = stmts;

    }

    @Override
    public boolean equals(final Object other){
        if(other instanceof Constructor){
            final Constructor constructor = (Constructor)other;
            return (vardecs.equals(constructor.vardecs) &&
                    exps.equals(constructor.exps) &&
                    stmts.equals(constructor.stmts));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return (vardecs.hashCode() +
                exps.hashCode() +
                stmts.hashCode());
    }

    @Override
    public String toString(){
        return ("Constructor(" +
                vardecs.toString() + ", " +
                exps.toString() + ", " +
                stmts.toString() + ")");
    }
}
