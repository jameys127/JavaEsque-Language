package com.jamey.compiler.Parser;

import java.util.List;
import java.util.Optional;

public class ClassDef {
    public final String classname;
    public final Optional<String> extend;
    public final List<VardecStmt> vardec;
    public final Constructor constructor;
    public final List<MethodDef> methoddef;

    public ClassDef(final String classname,
                    final Optional<String> extend,
                    final List<VardecStmt> vardec,
                    final Constructor constructor,
                    final List<MethodDef> methoddef){
        this.classname = classname;
        this.extend = extend;
        this.vardec = vardec;
        this.constructor = constructor;
        this.methoddef = methoddef;
    }

    @Override
    public boolean equals(final Object other){
        if(other instanceof ClassDef){
            final ClassDef def = (ClassDef)other;
            return (classname.equals(def.classname) &&
                    extend.equals(def.extend) &&
                    vardec.equals(def.vardec) &&
                    constructor.equals(def.constructor) &&
                    methoddef.equals(def.methoddef));
        }else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        return (classname.hashCode() +
                extend.hashCode() + 
                vardec.hashCode() +
                constructor.hashCode() +
                methoddef.hashCode());
    }

    @Override
    public String toString(){
        return("ClassDef(" + 
                classname.toString() + ", " +
                extend.toString() + ", " +
                vardec.toString() + ", " +
                constructor.toString() + ", " +
                methoddef.toString() + ")");
    }

}
