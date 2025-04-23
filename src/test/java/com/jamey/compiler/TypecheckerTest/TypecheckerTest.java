package com.jamey.compiler.TypecheckerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.jamey.compiler.Typechecker.*;
import com.jamey.compiler.Parser.*;
import com.jamey.compiler.Parser.MethodCallExp.MethodCall;

public class TypecheckerTest {
    private Map<Variable, Type> typeEnv = new HashMap<>();
    private Optional<ClassType> inClass = Optional.empty();
    
    @Test
    public void typecheckNum() throws TypecheckerErrorException{
        assertEquals(new IntType(), Typechecker.typecheckExp(new IntExp(7), typeEnv, Optional.empty()));
    }

    @Test
    public void typecheckBool() throws TypecheckerErrorException{
        assertEquals(new BoolType(), Typechecker.typecheckExp(new BooleanExp(true), typeEnv, Optional.empty()));
    }

    @Test
    public void typecheckVardecAndAssign() throws TypecheckerErrorException{
        Stmt stmt = new VardecStmt(new IntType(), "x");
        Stmt assign = new AssignStmt("x", new IntExp(5));
        Map<Variable, Type> map = new HashMap<Variable, Type>();
        map.put(new Variable("x"), new IntType());
        Map<Variable, Type> env = Typechecker.typecheckStmt(stmt, typeEnv, Optional.empty());
        env = Typechecker.typecheckStmt(assign, env, Optional.empty());
        
        assertEquals(map, env);
    }
    
    @Test
    public void testNewExpTypecheck() throws TypecheckerErrorException {
        ClassDef person = createPersonClass();
        Typechecker.putClassInMap(person.classname, person);

        ClassType personType = new ClassType("Person");
        
        List<Exp> args = new ArrayList<>();
        args.add(new IntExp(25)); // the age
        
        //new Person(25);
        NewExp newExp = new NewExp(personType, Optional.of(args));
        Type result = Typechecker.typecheckExp(newExp, typeEnv, inClass);
            
        assertEquals(personType, result);
    }    

    @Test
    public void testMethodCall() throws TypecheckerErrorException{
        ClassDef person = createPersonClass();
        Typechecker.putClassInMap(person.classname, person);

        ClassType personType = new ClassType("Person");
        
        Variable personVar = new Variable("person");
        typeEnv = Typechecker.addToMap(typeEnv, personVar, personType);
        
        //person.add5Years();
        VarExp personVarExp = new VarExp("person");
        List<Exp> emptyArgs = new ArrayList<>();
        List<MethodCall> methodCalls = new ArrayList<>();
        methodCalls.add(new MethodCall("add5Years", emptyArgs));
        
        MethodCallExp methodCallExp = new MethodCallExp(personVarExp, methodCalls);
        Type result = Typechecker.typecheckExp(methodCallExp, typeEnv, inClass);
            
        //should return an IntType
        assertTrue(result instanceof IntType);
    }
    @Test
    public void testMethodCallWhenCreated() throws TypecheckerErrorException{
        ClassDef person = createPersonClass();
        Typechecker.putClassInMap(person.classname, person);

        ClassType personType = new ClassType("Person");
        Variable personVar = new Variable("person");
        typeEnv = Typechecker.addToMap(typeEnv, personVar, personType);

        List<Exp> args = new ArrayList<>();
        args.add(new IntExp(25));
        NewExp newPerson = new NewExp(personType, Optional.of(args));

        List<Exp> emptyargs = new ArrayList<>();
        MethodCall call = new MethodCall("add5Years", emptyargs);
        List<MethodCall> methodcalls = new ArrayList<>();
        methodcalls.add(call);
        MethodCallExp methodcall = new MethodCallExp(newPerson, methodcalls);

        //new Person(25).add5Years();
        Type result = Typechecker.typecheckMethodCall(methodcall, typeEnv, inClass);
        assertTrue(result instanceof IntType);
    }

    private ClassDef createPersonClass() {
        /*
         * Person class with methods for getting age and setting age and adding 5 years
         * this should be used for most things
         * 'class' classname [extends otherclass] { (vardecs ; )* constructor methoddef* }
         */
        String className = "Person";
        Optional<String> extend = Optional.empty();
        
        //the variable declarations for the class person, for right now just the age
        List<VardecStmt> vardecs = new ArrayList<>();
        vardecs.add(new VardecStmt(new IntType(), "age"));
        
        //contructor "init (comma_vardec) { [optional super] stmt*}" gets an 'age' parameter
        List<VardecStmt> constructorParams = new ArrayList<>();
        constructorParams.add(new VardecStmt(new IntType(), "age"));
        
        List<Stmt> constructorBody = new ArrayList<>();
        //assignment statement that would be like this.age = age
        constructorBody.add(new AssignStmt("age", new VarExp("age")));
        
        Constructor constructor = new Constructor(constructorParams, Optional.empty(), constructorBody);
        
        //methods
        // 'method' methodname (comma_vardec) type { stmt* }
        // method getAge() Int { return age; }
        List<MethodDef> methods = new ArrayList<>();
        
        //getAge method
        String getAgeMethod = "getAge";
        Type intReturnType = new IntType();
        List<VardecStmt> getAgeParams = new ArrayList<>();
        List<Stmt> getAgeBody = new ArrayList<>();
        getAgeBody.add(new ReturnStmt(Optional.of(new VarExp("age"))));
        methods.add(new MethodDef(getAgeMethod, getAgeParams, intReturnType, getAgeBody));
        
        //setAge method
        String setAgeMethod = "setAge";
        Type voidReturnType = new VoidType();
        List<VardecStmt> setAgeParams = new ArrayList<>();
        setAgeParams.add(new VardecStmt(new IntType(), "newAge"));
        List<Stmt> setAgeBody = new ArrayList<>();
        setAgeBody.add(new AssignStmt("age", new VarExp("newAge")));
        methods.add(new MethodDef(setAgeMethod, setAgeParams, voidReturnType, setAgeBody));
        
        //add5Years method
        String add5YearsMethod = "add5Years";
        List<VardecStmt> add5YearsParams = new ArrayList<>();
        List<Stmt> add5YearsBody = new ArrayList<>();
        BinaryExp addingYears = new BinaryExp(
            new VarExp("age"),
            new PlusOp(),
            new IntExp(5)
        );
        add5YearsBody.add(new ReturnStmt(Optional.of(addingYears)));
        methods.add(new MethodDef(add5YearsMethod, add5YearsParams, intReturnType, add5YearsBody));
        
        return new ClassDef(className, extend, vardecs, constructor, methods);
    }

}
