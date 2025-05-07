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
    public static void assertTypesEqual(final Type expected, final Type received) throws TypecheckerErrorException{
        if(!(expected.equals(received)) && (expected instanceof ClassType && received instanceof ClassType)){
            ClassType receivedClass = (ClassType)received;
            ClassDef receivedClassDef = lookupClass(receivedClass.name());
            while(true){
                if(receivedClassDef.extend.isPresent()){
                    String parentclass = lookupClass(receivedClassDef.extend.get()).classname;
                    Type parentType = new ClassType(parentclass);
                    if(expected.equals(parentType)){
                        return;
                    }
                    receivedClassDef = lookupClass(receivedClassDef.extend.get());
                }else{
                    break;
                }
            }
        }
        if(!(expected.equals(received))){
            throw new TypecheckerErrorException("Types do not match; Excpected: "
                                                 + expected.toString() + ", Received: " + received.toString());
        }
    }
    /*
     * BinaryExp - done
     * BooleanExp - done
     * IntExp - done
     * MethodCallExp - done
     * NewExp - done
     * ParenExp - done
     * PrintlnExp - done
     * StrExp -currently never used in the language
     * ThisExp - done
     */

/*************************************************************************************************/
/*                                      EXP typechecking                                         */

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
        }else if(exp instanceof StrExp){
            return new StringType();
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
            typecheckExp(print.e(), typeEnv, inClass);
            return new VoidType();
        }else if(exp instanceof ThisExp){
            ThisExp thisexp = (ThisExp)exp;
            if(inClass.isPresent()){
                ClassType classType = inClass.get();
                if(thisexp.parentVar().isPresent()){
                    String var = thisexp.parentVar().get();
                    ClassDef classdef = lookupClass(classType.name());
                    while(true){
                        for(VardecStmt stmt : classdef.vardec){
                            if(stmt.name().equals(var)){
                                return stmt.type();
                            }
                        }
                        if(classdef.extend.isPresent()){
                            classdef = lookupClass(classdef.extend.get());
                        }else{
                            break;
                        }
                    }
                    throw new TypecheckerErrorException("Variable '" + thisexp.parentVar().get()
                    + "' not found in parent class '" + classdef.classname + "' or any children thereof");
                }else{
                    return classType;
                }
                
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
            return typecheckMethodCall(method, typeEnv, inClass);
        }
        else{
            assert(false);
            throw new TypecheckerErrorException("Unrecognized expression: " + exp.toString());
        }
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
        }else if((exp.op() instanceof GreaterThanOp ||
                  exp.op() instanceof GreaterThanOrEqualOp ||
                  exp.op() instanceof LessThanOp ||
                  exp.op() instanceof LessThanOrEqualOp) &&
                  left instanceof IntType &&
                  right instanceof IntType){
            return new BoolType();
        }else if(exp.op() instanceof EqualityOp){
            try{
                assertTypesEqual(left, right);
                return new BoolType();
            }catch (TypecheckerErrorException e){
                throw new TypecheckerErrorException("Differnt types in the equality check; " +
                 "left: " + left.toString() + " right: " + right.toString() + " error: " + e);
            }
        }
        else{
            throw new TypecheckerErrorException("No such binary operation recognized");
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
            if(actualClass.extend.isPresent()){
                ClassDef parentClass = lookupClass(actualClass.extend.get());
                while(true){
                    for(MethodDef defs : parentClass.methoddef){
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
                    if(parentClass.extend.isPresent()){
                        parentClass = lookupClass(parentClass.extend.get());
                    }else{
                        break;
                    }
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
/*                                     End EXP typechecking                                    */
/***********************************************************************************************/

    
/***********************************************************************************************/
/*                                      STMT typechecking                                      */
    /*
     * ExpStmt - done
     * VardecStmt - done
     * AssignStmt - done
     * WhileStmt - done
     * BreakStmt - done
     * ReturnStmt - done
     * IfStmt - done
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
                if(expType.equals(returnType.get())){
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
        if(stmt.thisTarget().isPresent()){
            ThisExp target = stmt.thisTarget().get();
            Type targetType = typecheckExp(target, typeEnv, inClass);
            assertTypesEqual(targetType, typecheckExp(stmt.e(), typeEnv, inClass));
            return typeEnv;
        }
        Variable variable = new Variable(stmt.name());
        if(typeEnv.containsKey(variable)){
            final Type expected = typeEnv.get(variable);
            assertTypesEqual(expected, typecheckExp(stmt.e(), typeEnv, inClass));
            Map<Variable, Type> newMap = addToMap(typeEnv, variable, typecheckExp(stmt.e(), typeEnv, inClass));
            return newMap;
        }else {
            throw new TypecheckerErrorException("Variable not in scope: " + stmt.name());
        }
    }


/*                                    End STMT typechecking                                    */
/***********************************************************************************************/


/***********************************************************************************************/
/*                                    MethodDef typechecking                                   */

    public static void typecheckMethodDef(final MethodDef method,
                                          final Optional<ClassType> inClass) 
                                          throws TypecheckerErrorException{
        Map<Variable, Type> newMap = new HashMap<>();
        for(VardecStmt vardecs : method.vars){
            newMap = typecheckStmt(vardecs, newMap, inClass, false, Optional.of(method.type));
        }
        for(Stmt body : method.stmts){
            newMap = typecheckStmt(body, newMap, inClass, false, Optional.of(method.type));
        }
        if(!(method.type instanceof VoidType) && !stmtsReturnProperly(method.stmts)){
            throw new TypecheckerErrorException("Method '" + method.methodname + 
            "' must definitely return a value of type '" + method.type.toString() + "'");
        }
    }
    public static boolean stmtsReturnProperly(List<Stmt> stmts){
        Boolean ifbody = true;
        Boolean elsebody = true;
        Boolean returnBool = null;
        for(Stmt stmt : stmts){
            if(stmt instanceof IfStmt ifstmt){
                if(!(ifstmt.stmt().get(ifstmt.stmt().size() - 1) instanceof ReturnStmt)){
                    ifbody = false;
                }
                if(ifstmt.elseStmt().isPresent()){
                    if(!(ifstmt.elseStmt().get().get(ifstmt.elseStmt().get().size() - 1) instanceof ReturnStmt)){
                        elsebody = false;
                    }
                    if(ifbody ^ elsebody){
                        returnBool = false;
                    }else{
                        returnBool = true;
                    }
                }
            }
        }
        if(!(stmts.get(stmts.size() - 1) instanceof ReturnStmt)){
            if(returnBool != null && returnBool == true){
                return true;
            }
            return false;
        }else{
            if(returnBool == null){
                return true;
            }else if(returnBool != null && returnBool == true){
                return true;
            }else{
                return false;
            }
        }
    }

/*                                    End MethodDef typechecking                               */
/***********************************************************************************************/


/***********************************************************************************************/
/*                                     Constructor typechecking                                */

    public static void typecheckConstructor(final ClassDef classdef)
                                            throws TypecheckerErrorException{
        Map<Variable, Type> typeEnv = new HashMap<>();
        List<VardecStmt> consVardecs = classdef.constructor.vardecs;
        for(VardecStmt var : consVardecs){
            typeEnv = addToMap(typeEnv, new Variable(var.name()), var.type());
        }
        Optional<ClassType> inClass = Optional.of(new ClassType(classdef.classname));
        if(classdef.extend.isPresent()){
            if(!classdef.constructor.exps.isPresent()){
                throw new TypecheckerErrorException("A super call is required when extending: "+classdef.classname);
            }
            ClassDef superClass = lookupClass(classdef.extend.get());
            if(superClass.constructor.vardecs.size() != classdef.constructor.exps.get().size()){
                throw new TypecheckerErrorException("Super call must contain same number of arguments as class '"
                 + superClass.classname +"' constructor");
            }
            List<Exp> superCallExps = classdef.constructor.exps.get();
            for(int i = 0; i < superClass.constructor.vardecs.size(); i++){
                Type argType = typecheckExp(superCallExps.get(i), typeEnv, inClass);
                try{
                    assertTypesEqual(superClass.constructor.vardecs.get(i).type(), argType);                   
                } catch (TypecheckerErrorException e){
                    throw new TypecheckerErrorException("supplied super args and super class contrustor don't match types:" + e);
                }
            }
        }else{
            if(classdef.constructor.exps.isPresent()){
                throw new TypecheckerErrorException("Called 'super' when not extending a class");
            }
        }
        for(Stmt stmt : classdef.constructor.stmts){
            typecheckStmt(stmt, typeEnv, inClass, false, Optional.empty());
        }
    }

/*                                    End Constructor typechecking                             */
/***********************************************************************************************/


/***********************************************************************************************/
/*                                      ClassDef typechecking                                  */

    public static void typecheckClassDef(final ClassDef classdef)throws TypecheckerErrorException{
        Map<Variable, Type> typeEnv = new HashMap<>();
        for(VardecStmt vardec : classdef.vardec){
            typeEnv = typecheckStmt(vardec, typeEnv, Optional.of(new ClassType(classdef.classname)), false, Optional.empty());
        }
        typecheckConstructor(classdef);
        for(MethodDef method : classdef.methoddef){
            typecheckMethodDef(method, Optional.of(new ClassType(classdef.classname)));
        }
        if(classdef.extend.isPresent()){
            methodOverriding(classdef, lookupClass(classdef.extend.get()));   
        }
    }
    public static void methodOverriding(ClassDef childClass, ClassDef parentClass) throws TypecheckerErrorException{
        for(MethodDef childmethod : childClass.methoddef){
            for(MethodDef parentmethod : parentClass.methoddef){
                if(childmethod.methodname.equals(parentmethod.methodname)){
                    if(!isSubtypeOrSameType(childmethod.type, parentmethod.type)){
                        throw new TypecheckerErrorException("Method '" + childmethod.methodname
                        + "' overrides with incompatible type");
                    }
                    if(childmethod.vars.size() != parentmethod.vars.size()){
                        throw new TypecheckerErrorException("number of arugments for overriding method '"+
                        childmethod.methodname + "' don't match number of parent method arguments");
                    }
                    for(int i = 0; i < childmethod.vars.size(); i++){
                        Type childtype = childmethod.vars.get(i).type();
                        Type parenttype = parentmethod.vars.get(i).type();
                        if(!childtype.equals(parenttype)){
                            throw new TypecheckerErrorException("argument types for overrideing method '"+
                            childmethod.methodname + "' don't match types for parent method arguments");
                        }
                    }
                }
            }
        }
        if(parentClass.extend.isPresent()){
            methodOverriding(childClass, lookupClass(parentClass.extend.get()));
        }
    }
    public static boolean isSubtypeOrSameType(Type childType, Type parentType){
        if(childType.equals(parentType)){
            return true;
        }
        if(childType instanceof ClassType && parentType instanceof ClassType){
            ClassType childClassType = (ClassType)childType;
            ClassType parentClassType = (ClassType)parentType;
            ClassDef childClass = lookupClass(childClassType.name());
            String childClassName = childClass.classname;
            while(true){
                ClassDef currentClass = lookupClass(childClassName);
                if(currentClass.extend.isPresent()){
                    String parentClassName = currentClass.extend.get();
                    if(parentClassName.equals(parentClassType.name())){
                        return true;
                    }
                    childClassName = parentClassName;
                }else {
                    break;
                }
            }
        }
        return false;
    }

/*                                    End ClassDef typechecking                                */
/***********************************************************************************************/

    public static void typecheckProgram(final Program program) throws TypecheckerErrorException{
        for(ClassDef classes : program.classdefs){
            putClassInMap(classes.classname, classes);
        }
        for(ClassDef classes : program.classdefs){
            typecheckClassDef(classes);
        }
        Map<Variable, Type> stmtMap = new HashMap<>();
        for(Stmt stmt : program.stmts){
            stmtMap = typecheckStmt(stmt, stmtMap, Optional.empty(), false, Optional.empty());
        }
    }
}
