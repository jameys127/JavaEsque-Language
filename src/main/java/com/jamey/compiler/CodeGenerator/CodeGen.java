package com.jamey.compiler.CodeGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import com.jamey.compiler.Parser.*;

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
        }else{
            assert(false);
            throw new CodeGenException("No such operator recognized: "+ op.toString());
        }
        writer.write(s);
    }
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
        }else if(exp instanceof BinaryExp binexp){
            writer.write("(");
            writeExp(binexp.l());
            writer.write(" ");
            writeOp(binexp.op());
            writer.write(" ");
            writeExp(binexp.r());
            writer.write(")");
        }else{
            assert(false);
            throw new CodeGenException("No such expression recognized: " + exp.toString());
        }
    }
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
            writer.write("}");
        }
        else if(stmt instanceof AssignStmt assign){
            writer.write(assign.name());
            writer.write(" = ");
            writeExp(assign.e());
            writer.write(";\n");
        }else{
            assert(false);
            throw new CodeGenException("No such statement recognized: " + stmt.toString());
        }
    }

    public void close() throws IOException{
        writer.close();
    }
    public void writeProgram(final Program program)throws IOException, CodeGenException{
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
