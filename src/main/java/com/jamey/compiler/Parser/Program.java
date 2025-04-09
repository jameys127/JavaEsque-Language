package com.jamey.compiler.Parser;

import java.util.List;

public class Program {
    public final List<ClassDef> classdefs;
    public final List<Stmt> stmts;

    public Program(final List<ClassDef> classdefs,
                   final List<Stmt> stmts){
        this.classdefs = classdefs;
        this.stmts = stmts;
    }

    @Override
    public boolean equals(final Object other){
        if(other instanceof Program){
            final Program def = (Program)other;
            return (classdefs.equals(def.classdefs) &&
                    stmts.equals(def.stmts));
        }else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return (classdefs.hashCode() +
                stmts.hashCode());
    }

    @Override
    public String toString(){
        return ("Program(" +
                classdefs.toString() + ", " +
                stmts.toString() + ")");
    }
    
}
