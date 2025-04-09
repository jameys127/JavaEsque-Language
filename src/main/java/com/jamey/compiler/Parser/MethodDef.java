package com.jamey.compiler.Parser;

import java.util.List;

public class MethodDef {
    public final String methodname;
    public final List<VardecStmt> vars;
    public final Type type;
    public final List<Stmt> stmts;

    public MethodDef(final String methodname,
                     final List<VardecStmt> vars,
                     final Type type,
                     final List<Stmt> stmts){
        this.methodname = methodname;
        this.vars = vars;
        this.type = type;
        this.stmts = stmts;
    }

    @Override
    public boolean equals(final Object other){
        if(other instanceof MethodDef){
            final MethodDef method = (MethodDef)other;
            return (methodname.equals(method.methodname) &&
                    vars.equals(method.vars) &&
                    type.equals(method.type) &&
                    stmts.equals(method.stmts));
        }else {
            return false;
        }
    }

    @Override
    public int hashCode(){
        return (methodname.hashCode() +
                vars.hashCode() + 
                type.hashCode() +
                stmts.hashCode());
    }

    @Override
    public String toString(){
        return ("MethodDef(" + 
                methodname.toString() + ", " +
                vars.toString() + ", " +
                type.toString() + ", " +
                stmts.toString() + ")");
    }

}
