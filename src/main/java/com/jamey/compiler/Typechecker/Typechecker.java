package com.jamey.compiler.Typechecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jamey.compiler.Parser.*;

public class Typechecker {

    private static Map<String, ClassDef> classDefinitions = new HashMap<>();

    public static void putClassInMap(final String classname, final ClassDef classdef){
        classDefinitions.put(classname, classdef);
    }
    public static ClassDef lookupClass(final String classname){
        return classDefinitions.get(classname);
    }



    public static Map<Variable, Type> addToMap(final Map<Variable, Type> typeEnv,
                                               final Variable variable,
                                               final Type type){
        final Map<Variable, Type> retval = new HashMap<Variable, Type>();
        retval.putAll(typeEnv);
        retval.put(variable, type);
        return retval;
    }

    public static Type typecheckBin(final BinaryExp exp,
                                    final Map<Variable, Type> typeEnv)
                                    throws TypecheckerErrorException{
        final Type left = typecheckExp(exp.l(), typeEnv);
        final Type right = typecheckExp(exp.r(), typeEnv);
        if((exp.op() instanceof PlusOp ||
            exp.op() instanceof MinusOp ||
            exp.op() instanceof MultOp ||
            exp.op() instanceof DivOp) &&
            left instanceof IntType &&
            right instanceof IntType){
            return new IntType();
        }else{
            throw new TypecheckerErrorException("No such binary operation recognized");
        }
    }
    /*
     * BinaryExp - done
     * BooleanExp - done
     * IntExp - done
     * MethodCallExp
     * NewExp
     * ParenExp - done
     * PrintlnExp - done
     * StrExp -currently never used in the language
     * ThisExp
     */
    public static Type typecheckExp(final Exp exp, 
                                    final Map<Variable, Type> typeEnv)
                                     throws TypecheckerErrorException{
        if(exp instanceof IntExp){
            return new IntType();
        }else if(exp instanceof BooleanExp){
            return new BoolType();
        } else if (exp instanceof VarExp){
            final Variable variable = new Variable(((VarExp)exp).name());
            if(typeEnv.containsKey(variable)){
                return typeEnv.get(variable);
            }else {
                throw new TypecheckerErrorException("Variable not in scope: " + variable.toString());
            }
        }else if(exp instanceof BinaryExp){
            return typecheckBin((BinaryExp)exp, typeEnv);
        }else if(exp instanceof ParenExp){
            ParenExp paren = (ParenExp)exp;
            return typecheckExp(paren.e(), typeEnv);
        }else if(exp instanceof PrintlnExp){
            /*
             * Might need to change this depending on what I can print
             */
            PrintlnExp print = (PrintlnExp)exp;
            return typecheckExp(print.e(), typeEnv);
        }else if(exp instanceof NewExp){
            NewExp newExp = (NewExp)exp;
            String classname = newExp.type().name();
            ClassDef classDef = lookupClass(classname);
            if(classDef == null){
                throw new TypecheckerErrorException("No such class '" + classname + "' exists");
            }
            Optional<List<Exp>> args = newExp.exps();
            if(args.isPresent()){
                List<Exp> argList = args.get();
                List<VardecStmt> constructorVardecs = classDef.constructor.vardecs;
                if(argList.size() != constructorVardecs.size()){
                    throw new TypecheckerErrorException("Wrong number of arguments for constructor of '" + classname + "'");
                }
                for(int i = 0; i < constructorVardecs.size(); i++){
                    Type arg = typecheckExp(argList.get(i), typeEnv);
                    Type conVar = constructorVardecs.get(i).type();
                    if(!conVar.equals(arg)){
                        throw new TypecheckerErrorException("Initializing argument doesn't match type in the constructor");
                    }
                }
            }else{
                List<VardecStmt> constructorVardecs = classDef.constructor.vardecs;
                if(!constructorVardecs.isEmpty()){
                    throw new TypecheckerErrorException("Class constructor for '" + classname + "' requires arguments");
                }
            }
            return newExp.type();
        }
        else{
            assert(false);
            throw new TypecheckerErrorException("Unrecognized expression: " + exp.toString());
        }
    }

    public static void assertTypesEqual(final Type expected, final Type received) throws TypecheckerErrorException{
        if(!(expected.equals(received))){
            throw new TypecheckerErrorException("Types do not match; Excpected: "
                                                 + expected.toString() + ", Received: " + received.toString());
        }
    }

    public static Map<Variable, Type> typecheckVardec(final VardecStmt stmt,
                                        final Map<Variable, Type> typeEnv)
                                        throws TypecheckerErrorException{
        Variable variable = new Variable(stmt.name());
        Type type = stmt.type();
        return addToMap(typeEnv, variable, type);
    }
    public static Map<Variable, Type> typecheckWhile(final WhileStmt stmt,
                                                     final Map<Variable, Type> typeEnv) 
                                                     throws TypecheckerErrorException {
        assertTypesEqual(new BoolType(), typecheckExp(stmt.e(), typeEnv));
        for(Stmt body : stmt.stmt()){
            typecheckStmt(body, typeEnv);
        }
        return typeEnv;
    }
    public static Map<Variable, Type> typecheckAssign(final AssignStmt stmt,
                                                      final Map<Variable, Type> typeEnv)
                                                      throws TypecheckerErrorException{
        Variable variable = new Variable(stmt.name());
        if(typeEnv.containsKey(variable)){
            final Type expected = typeEnv.get(variable);
            assertTypesEqual(expected, typecheckExp(stmt.e(), typeEnv));
            return typeEnv;
        }else {
            throw new TypecheckerErrorException("Variable not in scope: " + stmt.name());
        }
    }
    
    /*
     * ExpStmt - done
     * VardecStmt - done
     * AssignStmt - done
     * WhileStmt - done
     * BreakStmt
     * ReturnStmt
     * IfStmt
     * 
     */
    public static Map<Variable, Type> typecheckStmt(final Stmt stmt,
                                    final Map<Variable, Type> typeEnv)
                                    throws TypecheckerErrorException{
        if(stmt instanceof VardecStmt){
            return typecheckVardec((VardecStmt)stmt, typeEnv);
        }else if(stmt instanceof WhileStmt){
            return typecheckWhile((WhileStmt)stmt, typeEnv);
        }else if(stmt instanceof AssignStmt){
            return typecheckAssign((AssignStmt)stmt, typeEnv);
        }else if(stmt instanceof ExpStmt){
            ExpStmt expstmt = (ExpStmt)stmt;
            typecheckExp(expstmt.e(), typeEnv);
            return typeEnv;
        }
        else {
            throw new TypecheckerErrorException("Unrecognized Statement: " + stmt.toString());
        }
    }

    //for now this only checks the stmts and not all of the class definitions
    public static void typecheckProgram(final Program program) throws TypecheckerErrorException{
        Map<Variable, Type> typeEnv = new HashMap<Variable, Type>();
        for(final Stmt stmt : program.stmts){
            typeEnv = typecheckStmt(stmt, typeEnv);
        }
    }
}
