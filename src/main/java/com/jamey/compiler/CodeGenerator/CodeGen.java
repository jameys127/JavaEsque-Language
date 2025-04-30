package com.jamey.compiler.CodeGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.BufferedWriter;
import com.jamey.compiler.Parser.*;
import com.jamey.compiler.Parser.MethodCallExp.MethodCall;

public class CodeGen {
    private final BufferedWriter writer;
    public CodeGen(final File outputFile)throws IOException{
        writer = new BufferedWriter(new FileWriter(outputFile));
    }

    public void writeOp(final Op op)throws IOException, CodeGenException{
        String s = null;
        if(op instanceof PlusOp){
            s = "+";
        }else if(op instanceof MinusOp){
            s = "-";
        }else if(op instanceof MultOp){
            s = "*";
        }else if(op instanceof DivOp){
            s = "/";
        }else if(op instanceof LessThanOp){
            s = "<";
        }else if(op instanceof LessThanOrEqualOp){
            s = "<=";
        }else if(op instanceof GreaterThanOp){
            s = ">";
        }else if(op instanceof GreaterThanOrEqualOp){
            s = ">=";
        }else if(op instanceof EqualityOp){
            s = "==";
        }else{
            assert(false);
            throw new CodeGenException("No such operator recognized: "+ op.toString());
        }
        writer.write(s);
    }
    /*
     * BinaryExp - done
     * BooleanExp - done
     * IntExp - done
     * MethodCallExp - done
     * NewExp - done
     * ParenExp - done
     * PrintlnExp - done
     * ThisExp - done
     * StrExp
     */
    public void writeExp(final Exp exp)throws IOException, CodeGenException{
        if(exp instanceof IntExp intexp){
            writer.write(Integer.toString(intexp.value()));
        }else if(exp instanceof BooleanExp boolexp){
            if(boolexp.bool()){
                writer.write("true");
            }else{
                writer.write("false");
            }
        }else if(exp instanceof VarExp varexp){
            writer.write(varexp.name());
        }else if(exp instanceof StrExp strexp){
            writer.write("\"");
            writer.write(strexp.string());
            writer.write("\"");
        }else if(exp instanceof BinaryExp binexp){
            writer.write("(");
            writeExp(binexp.l());
            writer.write(" ");
            writeOp(binexp.op());
            writer.write(" ");
            writeExp(binexp.r());
            writer.write(")");
        }else if(exp instanceof ParenExp paren){
            writer.write("(");
            writeExp(paren.e());
            writer.write(")");
        }else if(exp instanceof PrintlnExp print){
            writer.write("console.log(");
            writeExp(print.e());
            writer.write(")");
        }else if(exp instanceof ThisExp thisexp){
            writer.write("this.");
            writer.write(thisexp.parentVar().get());
        }else if(exp instanceof MethodCallExp method){
            int size = method.methodCalls().size();
            writeExp(method.e());
            writer.write(".");
            for(MethodCall methods : method.methodCalls()){
                writer.write(methods.name());
                writer.write("(");
                writeCommaVariable(methods.exps());
                writer.write(")");
                if(size > 1){
                    writer.write(".");
                }
                size--;
            }
        }else if(exp instanceof NewExp newexp){
            writer.write("new ");
            writer.write(newexp.type().name());
            writer.write("(");
            if(newexp.exps().isPresent()){
                writeCommaVariable(newexp.exps().get());
            }
            writer.write(")");
        }

        else{
            assert(false);
            throw new CodeGenException("No such expression recognized: " + exp.toString());
        }
    }
    /*
     * ExpStmt - done
     * VardecStmt - done
     * AssignStmt - done
     * WhileStmt - done
     * BreakStmt - done
     * ReturnStmt - done
     * IfStmt - done
     */
    public void writeStmt(final Stmt stmt)throws IOException, CodeGenException{
        //vardec: Type variable;
        //JavaScript: let variable;
        if(stmt instanceof VardecStmt vardec){
            writer.write("let ");
            writer.write(vardec.name());
            writer.write(";\n");
        }
        //while(expression){statement;}
        //
        else if(stmt instanceof WhileStmt whilestmt){
            writer.write("while (");
            writeExp(whilestmt.e());
            writer.write(") { ");
            for(Stmt other : whilestmt.stmt()){
                writeStmt(other);
            }
            writer.write("}\n");
        }
        else if(stmt instanceof AssignStmt assign){
            if(assign.thisTarget().isPresent()){
                writer.write("this.");
            }
            writer.write(assign.name());
            writer.write(" = ");
            writeExp(assign.e());
            writer.write(";\n");
        }else if(stmt instanceof ExpStmt expstmt){
            writeExp(expstmt.e());
            writer.write(";\n");
        }else if(stmt instanceof BreakStmt){
            writer.write("break;\n");
        }else if(stmt instanceof ReturnStmt returnstmt){
            if(!returnstmt.e().isPresent()){
                writer.write("return;\n");
            }else{
                writer.write("return ");
                writeExp(returnstmt.e().get());
                writer.write(";\n");
            }
        } else if(stmt instanceof IfStmt ifstmt){
            writer.write("if(");
            writeExp(ifstmt.e());
            writer.write(") {\n");
            for (Stmt body : ifstmt.stmt()){
                writeStmt(body);
            }
            writer.write("}\n");
            if(ifstmt.elseStmt().isPresent()){
                writer.write("else {\n");
                for(Stmt elsebody : ifstmt.elseStmt().get()){
                    writeStmt(elsebody);
                }
                writer.write("}\n");
            }
        }
        else{
            assert(false);
            throw new CodeGenException("No such statement recognized: " + stmt.toString());
        }
    }
    public void writeCommaVariable(List<?> list )throws IOException, CodeGenException{
        int size = list.size();
        for (Object object : list){
            if(object instanceof VardecStmt vardec){
                writer.write(vardec.name());
                if(size > 1){
                    writer.write(", ");
                }
                size--;
            }
            if(object instanceof Exp exp){
                writeExp(exp);
                if(size > 1){
                    writer.write(", ");
                }
                size--;
            }
        }
    }

    public void writeMethod(final MethodDef method) throws IOException, CodeGenException{
        writer.write(method.methodname);
        writer.write("(");
        writeCommaVariable(method.vars);
        writer.write(") {\n");
        for(Stmt stmt : method.stmts){
            writeStmt(stmt);
        }
        writer.write("}\n");

    }

    public void writeClass(final ClassDef classdef) throws IOException, CodeGenException{
        writer.write("class ");
        writer.write(classdef.classname);
        if(classdef.extend.isPresent()){
            writer.write(" extends ");
            writer.write(classdef.extend.get());
        }
        writer.write(" {\n");
        writer.write("constructor(");
        Constructor constructor = classdef.constructor;
        writeCommaVariable(constructor.vardecs);
        writer.write(") {\n");
        if(constructor.exps.isPresent()){
            writer.write("super(");
            writeCommaVariable(constructor.exps.get());
            writer.write(");\n");
        }
        for(Stmt stmt : constructor.stmts){
            writeStmt(stmt);
        }
        writer.write("}\n");
        for(MethodDef method : classdef.methoddef){
            writeMethod(method);
        }
        writer.write("}\n");
    }

    public void close() throws IOException{
        writer.close();
    }
    public void writeProgram(final Program program)throws IOException, CodeGenException{
        for(final ClassDef classdef : program.classdefs){
            writeClass(classdef);
        }
        for(final Stmt stmt : program.stmts){
            writeStmt(stmt);
        }
    }
    public static void writeProgram(final Program program, final File outputFile) throws IOException, CodeGenException{
        final CodeGen codegen = new CodeGen(outputFile);
        try{
            codegen.writeProgram(program);;
        }finally{
            codegen.close();
        }
    }
}
