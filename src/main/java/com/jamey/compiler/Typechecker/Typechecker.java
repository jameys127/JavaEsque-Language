package com.jamey.compiler.Typechecker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.jamey.compiler.Parser.*;
import com.jamey.compiler.Parser.MethodCallExp.MethodCall;

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
                                    final Map<Variable, Type> typeEnv,
                                    final Optional<ClassType> inClass)
                                    throws TypecheckerErrorException{
        final Type left = typecheckExp(exp.l(), typeEnv, inClass);
        final Type right = typecheckExp(exp.r(), typeEnv, inClass);
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
                                    final Map<Variable, Type> typeEnv,
                                    final Optional<ClassType> inClass)
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
            return typecheckBin((BinaryExp)exp, typeEnv, inClass);
        }else if(exp instanceof ParenExp){
            ParenExp paren = (ParenExp)exp;
            return typecheckExp(paren.e(), typeEnv, inClass);
        }else if(exp instanceof PrintlnExp){
            /*
             * Might need to change this depending on what I can print
             */
            PrintlnExp print = (PrintlnExp)exp;
            return typecheckExp(print.e(), typeEnv, inClass);
        }else if(exp instanceof ThisExp){
            if(inClass.isPresent()){
                ClassType classType = inClass.get();
                return classType;
            }else{
                throw new TypecheckerErrorException("Using 'this' when not in a class");
            }
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
                    Type arg = typecheckExp(argList.get(i), typeEnv, inClass);
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
        }else if(exp instanceof MethodCallExp method){
            /*1. get the object type
             * 2.check to see if the method is present within the class
             * 3.can have multple method calls
             * 4. final type
             */
            return typecheckMethodCall(method, typeEnv, inClass);

        }
        else{
            assert(false);
            throw new TypecheckerErrorException("Unrecognized expression: " + exp.toString());
        }
    }
    public static Type typecheckMethodCall(final MethodCallExp exp, 
                                           final Map<Variable, Type> typeEnv,
                                           final Optional<ClassType> inClass)
                                           throws TypecheckerErrorException{
        Type objectType = typecheckExp(exp.e(), typeEnv, inClass);
        if(!(objectType instanceof ClassType)){
            throw new TypecheckerErrorException("Calling a method on object that isn't a class");
        }
        ClassType objectAsClassType = (ClassType)objectType;
        List<MethodCall> listOfCalls = exp.methodCalls();
        ClassDef actualClass = lookupClass(objectAsClassType.name());

        for(MethodCall methods : listOfCalls){
            String methodname = methods.name();
            List<Exp> arguments = methods.exps();
            MethodDef matchingMethod = null;
            
            for(MethodDef defs : actualClass.methoddef){
                if(methodname.equals(defs.methodname)){
                    if(arguments.size() != defs.vars.size()){
                        throw new TypecheckerErrorException("Method '" + defs.methodname + "' expects "
                        + defs.vars.size() + " arguments; Got: " + arguments.size());
                    }
                    for(int i = 0; i < defs.vars.size(); i++){
                        Type argType = typecheckExp(arguments.get(i), typeEnv, inClass);
                        Type paramType = defs.vars.get(i).type();
                        if(!argType.equals(paramType)){
                            throw new TypecheckerErrorException("Arguments provided don't match required arguments in: " + defs.methodname);
                        }
                    }
                    matchingMethod = defs;
                    break;
                }
            }
            if(matchingMethod == null){
                throw new TypecheckerErrorException("Method '" + methodname + 
                "' is not a valid method for object '" + objectAsClassType.toString() + "'");
            }
            objectType = matchingMethod.type;
            if(objectType instanceof ClassType){
                objectAsClassType = (ClassType)objectType;
                actualClass = lookupClass(objectAsClassType.name());
            }
        }
        return objectType;
        
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
                                                     final Map<Variable, Type> typeEnv,
                                                     final Optional<ClassType> inClass,
                                                     Boolean inLoop,
                                                     final Optional<Type> returnType)
                                                     throws TypecheckerErrorException {
        inLoop = true;
        assertTypesEqual(new BoolType(), typecheckExp(stmt.e(), typeEnv, inClass));
        for(Stmt body : stmt.stmt()){
            typecheckStmt(body, typeEnv, inClass, inLoop, returnType);
        }
        return typeEnv;
    }
    public static Map<Variable, Type> typecheckAssign(final AssignStmt stmt,
                                                      final Map<Variable, Type> typeEnv,
                                                      final Optional<ClassType> inClass)
                                                      throws TypecheckerErrorException{
        Variable variable = new Variable(stmt.name());
        if(typeEnv.containsKey(variable)){
            final Type expected = typeEnv.get(variable);
            assertTypesEqual(expected, typecheckExp(stmt.e(), typeEnv, inClass));
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
     * BreakStmt - done
     * ReturnStmt - done
     * IfStmt
     * 
     */
    public static Map<Variable, Type> typecheckStmt(final Stmt stmt,
                                    final Map<Variable, Type> typeEnv,
                                    final Optional<ClassType> inClass,
                                    Boolean inLoop,
                                    final Optional<Type> returnType)
                                    throws TypecheckerErrorException{
        if(stmt instanceof VardecStmt){
            return typecheckVardec((VardecStmt)stmt, typeEnv);
        }else if(stmt instanceof WhileStmt){
            return typecheckWhile((WhileStmt)stmt, typeEnv, inClass, inLoop, returnType);
        }else if(stmt instanceof AssignStmt){
            return typecheckAssign((AssignStmt)stmt, typeEnv, inClass);
        }else if(stmt instanceof ExpStmt){
            ExpStmt expstmt = (ExpStmt)stmt;
            typecheckExp(expstmt.e(), typeEnv, inClass);
            return typeEnv;
        }else if(stmt instanceof BreakStmt){
            if(inLoop == false){
                throw new TypecheckerErrorException("Using 'break' while not in a loop");
            }
            return typeEnv;
        }else if(stmt instanceof ReturnStmt){
            ReturnStmt returnStmt = (ReturnStmt)stmt;
            if(!returnType.isPresent()){
                throw new TypecheckerErrorException("Using 'return' while not inside a method");
            }
            if(returnStmt.e().isPresent()){
                Exp returnExp = returnStmt.e().get();
                Type expType = typecheckExp(returnExp, typeEnv, inClass);
                if(expType == returnType.get()){
                    return typeEnv;
                }else{
                    throw new TypecheckerErrorException("return expression is of type '" + expType.toString()
                    + "' when the method requires type '" + returnType.get().toString() + "'" );
                }
            }else{
                if(!returnType.get().equals(new VoidType())){
                    throw new TypecheckerErrorException("Returning 'Void' in a method that returns a non-Void type");
                }
                return typeEnv;
            }
        }else if(stmt instanceof IfStmt){
            IfStmt ifstmt = (IfStmt)stmt;
            Type condition = typecheckExp(ifstmt.e(), typeEnv, inClass);
            if(!(condition.equals(new BoolType()))){
                throw new TypecheckerErrorException("Condition in if statement must return a boolean");
            }
            for(Stmt body : ifstmt.stmt()){
                typecheckStmt(body, typeEnv, inClass, inLoop, returnType);
            }
            if(ifstmt.elseStmt().isPresent()){
                List<Stmt> elsebody = ifstmt.elseStmt().get();
                for(Stmt elsestmts : elsebody){
                    typecheckStmt(elsestmts, typeEnv, inClass, inLoop, returnType);
                }
            }
            return typeEnv;
        }
        else {
            throw new TypecheckerErrorException("Unrecognized Statement: " + stmt.toString());
        }
    }

    //for now this only checks the stmts and not all of the class definitions
    public static void typecheckProgram(final Program program) throws TypecheckerErrorException{

        // Map<Variable, Type> typeEnv = new HashMap<Variable, Type>();
        // for(final Stmt stmt : program.stmts){
        //     typeEnv = typecheckStmt(stmt, typeEnv);
        // }
    }
}
