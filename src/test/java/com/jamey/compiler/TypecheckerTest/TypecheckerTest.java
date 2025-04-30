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
import com.jamey.compiler.Lexer.Token;
import com.jamey.compiler.Lexer.Tokenizer;
import com.jamey.compiler.Lexer.TokenizerException;
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
        Stmt assign = new AssignStmt(Optional.empty(), "x", new IntExp(5));
        Map<Variable, Type> map = new HashMap<Variable, Type>();
        map.put(new Variable("x"), new IntType());
        Map<Variable, Type> env = Typechecker.typecheckStmt(stmt, typeEnv, Optional.empty(), false, Optional.empty());
        env = Typechecker.typecheckStmt(assign, env, Optional.empty(), false, Optional.empty());
        
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
    public void testStringLiteralsTypechecking() throws TypecheckerErrorException{
        Stmt stmt = new VardecStmt(new StringType(), "x");
        Stmt assign = new AssignStmt(Optional.empty(), "x", new StrExp("something"));
        Map<Variable, Type> map = new HashMap<Variable, Type>();
        map.put(new Variable("x"), new StringType());
        Map<Variable, Type> env = Typechecker.typecheckStmt(stmt, map, Optional.empty(), false, Optional.empty());
        env = Typechecker.typecheckStmt(assign, env, Optional.empty(), false, Optional.empty());
        assertEquals(map, env);                
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
    @Test
    public void testStmtVardec() throws TypecheckerErrorException{
        VardecStmt vardec = new VardecStmt(new ClassType("Person"), "James");
        Map<Variable, Type> expected = new HashMap<>();
        expected.put(new Variable("James"), new ClassType("Person"));
        Map<Variable, Type> result = Typechecker.typecheckStmt(vardec, typeEnv, inClass, false, Optional.empty());
        assertEquals(expected, result);
    }
    @Test
    public void testStmtAssign() throws TypecheckerErrorException{
        VardecStmt vardec = new VardecStmt(new IntType(), "James");
        AssignStmt assign = new AssignStmt(Optional.empty(), "James", new IntExp(5));
        Map<Variable, Type> newMap = Typechecker.typecheckStmt(vardec, typeEnv, inClass, false, Optional.empty());
        Typechecker.typecheckStmt(assign, newMap, inClass, false, Optional.empty());
    }
    @Test
    public void testStmtAssignClassObject() throws TypecheckerErrorException{
        VardecStmt vardec = new VardecStmt(new ClassType("Person"), "James");
        List<Exp> args = new ArrayList<>();
        Exp exp = new IntExp(25);
        args.add(exp);
        AssignStmt assign = new AssignStmt(Optional.empty(), "James", new NewExp(new ClassType("Person"), Optional.of(args)));
        Typechecker.putClassInMap("Person", createPersonClass());
        Map<Variable, Type> newMap = Typechecker.typecheckStmt(vardec, typeEnv, inClass, false, Optional.empty());
        Typechecker.typecheckStmt(assign, newMap, inClass, false, Optional.empty());
    }
    @Test
    public void testTypecheckingWholeClass() throws TypecheckerErrorException{
        ClassDef person = createPersonClass();
        Typechecker.typecheckClassDef(person);
    }
    @Test
    public void testTypecheckingProgramWithInheritingClassDefs()throws TypecheckerErrorException{
        ClassDef person = createPersonClass();
        ClassDef derek = createDerekClass();
        List<ClassDef> listOfClasses = new ArrayList<>();
        List<Stmt> listOfStmts = new ArrayList<>();
        listOfClasses.add(person);
        listOfClasses.add(derek);
        Stmt stmt = new ExpStmt(new PrintlnExp(new IntExp(3)));
        listOfStmts.add(stmt);
        Program program = new Program(listOfClasses, listOfStmts);
        Typechecker.typecheckProgram(program);
        
    }
    @Test
    public void testNewExpWithNoParameters()throws TypecheckerErrorException{
        String classname = "MyClass";
        List<VardecStmt> vardecs = new ArrayList<>();
        
        List<VardecStmt> constructorParams = new ArrayList<>();

        List<Stmt> constructorBody = new ArrayList<>();
        Constructor constructor = new Constructor(constructorParams, Optional.empty(), constructorBody);

        List<MethodDef> methods = new ArrayList<>();
        String printMethod = "printing";
        Type returnVoidType = new VoidType();
        List<VardecStmt> params = new ArrayList<>();
        List<Stmt> prinStmts = new ArrayList<>();
        prinStmts.add(new ExpStmt(new PrintlnExp(new IntExp(5))));
        methods.add(new MethodDef(printMethod, params, returnVoidType, prinStmts));
        ClassDef myclass = new ClassDef(classname, Optional.empty(), vardecs, constructor, methods);
        
        Typechecker.putClassInMap(classname, myclass);
        
        NewExp newcall = new NewExp(new ClassType(classname), Optional.empty());
        Type result = Typechecker.typecheckExp(newcall, typeEnv, inClass);
        assertEquals(new ClassType(classname), result);


    }
    @Test
    public void testIfCondition()throws TokenizerException, ParserException, TypecheckerErrorException{
        String input = """
                boolean condition;
                condition = false;
                if(false){
                    println(2);
                }else{
                    println(3);
                }
                """;
        Tokenizer tokenizer = new Tokenizer(input);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        Program program = parser.parseWholeProgram();
        Typechecker.typecheckProgram(program); 
    }
    @Test
    public void testWhileLoop()throws TokenizerException, ParserException, TypecheckerErrorException{
        String input = """
                boolean stuff;
                stuff = true;
                int x;
                x = 5;
                while(stuff){
                    x = x + 1;
                    break;
                }

                """;
        Tokenizer tokenizer = new Tokenizer(input);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        Program program = parser.parseWholeProgram();
        Typechecker.typecheckProgram(program); 
    }
    @Test
    public void testingWithTheWholeCompiler()throws TokenizerException, ParserException, TypecheckerErrorException{
        String input = """
            class Animal {
              init() {}
              method speak() void { return; }
            }
            class Cat extends Animal {
              init() { super(); }
              method meow(int x, int y) void { return println(1); }
            }
            class Mouse extends Cat {
                init() { super(); }
                method meow(int d, int f) void { int c; c = d + f; println(c); }
                method squeak(int x, int y) void {return println(2); }
            }
            Animal animal;
            Animal cat;
            Cat mouse;
            animal = new Animal();
            cat = new Cat();
            mouse = new Mouse();
            cat.speak();
            mouse.speak();
            mouse.meow(5, 6);
            mouse.squeak(1, 4);
            """;
    
        Tokenizer tokenizer = new Tokenizer(input);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        Program result = parser.parseWholeProgram();
        Typechecker.typecheckProgram(result);
    }
    @Test
    public void testingExtendFurther()throws TokenizerException, ParserException, TypecheckerErrorException{
        String input = """
                class Person {
                    int x;
                    init(int y) {this.x = y;}
                    method speak() int { return this.x; }
                }
                class Derek extends Person{
                    boolean stuff;
                    init(int z, boolean l) { super(z); this.stuff = l;}
                    method stuff() boolean {return this.stuff;}
                }
                Person person;
                boolean trueOrFalse;
                person = new Derek(2, false);
                trueOrFalse = person.stuff();
                """;
        Tokenizer tokenizer = new Tokenizer(input);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        Program result = parser.parseWholeProgram();
        Typechecker.typecheckProgram(result);
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
        constructorBody.add(new AssignStmt(Optional.of(new ThisExp(Optional.of("age"))), "age", new VarExp("age")));
        
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
        getAgeBody.add(new ReturnStmt(Optional.of(new ThisExp(Optional.of("age")))));
        methods.add(new MethodDef(getAgeMethod, getAgeParams, intReturnType, getAgeBody));
        
        //setAge method
        String setAgeMethod = "setAge";
        Type voidReturnType = new VoidType();
        List<VardecStmt> setAgeParams = new ArrayList<>();
        setAgeParams.add(new VardecStmt(new IntType(), "newAge"));
        List<Stmt> setAgeBody = new ArrayList<>();
        setAgeBody.add(new AssignStmt(Optional.of(new ThisExp(Optional.of("age"))), "age", new VarExp("newAge")));
        methods.add(new MethodDef(setAgeMethod, setAgeParams, voidReturnType, setAgeBody));
        
        //add5Years method
        String add5YearsMethod = "add5Years";
        List<VardecStmt> add5YearsParams = new ArrayList<>();
        List<Stmt> add5YearsBody = new ArrayList<>();
        BinaryExp addingYears = new BinaryExp(
            new ThisExp(Optional.of("age")),
            new PlusOp(),
            new IntExp(5)
        );
        add5YearsBody.add(new ReturnStmt(Optional.of(addingYears)));
        methods.add(new MethodDef(add5YearsMethod, add5YearsParams, intReturnType, add5YearsBody));
        
        return new ClassDef(className, extend, vardecs, constructor, methods);
    }
    private ClassDef createDerekClass(){
        String classname = "Derek";
        Optional<String> extend = Optional.of("Person");
        List<VardecStmt> vardecs = new ArrayList<>();
        vardecs.add(new VardecStmt(new BoolType(), "deceased"));
        
        List<VardecStmt> constructorParams = new ArrayList<>();
        constructorParams.add(new VardecStmt(new BoolType(), "deceased"));

        List<Exp> superexps = new ArrayList<>();
        superexps.add(new IntExp(30));

        List<Stmt> constructorBody = new ArrayList<>();
        constructorBody.add(new AssignStmt(Optional.of(new ThisExp(Optional.of("deceased"))), "deceased", new VarExp("deceased")));

        Constructor constructor = new Constructor(constructorParams, Optional.of(superexps), constructorBody);

        List<MethodDef> methods = new ArrayList<>();

        //isDeceased method
        String isDeceasedMethod = "isDeceased";
        Type boolReturnType = new BoolType();
        List<VardecStmt> isDeceasedParams = new ArrayList<>();
        List<Stmt> isDeceasedBody = new ArrayList<>();
        isDeceasedBody.add(new ReturnStmt(Optional.of(new ThisExp(Optional.of("deceased")))));
        methods.add(new MethodDef(isDeceasedMethod, isDeceasedParams, boolReturnType, isDeceasedBody));

        String add5YearsMethod = "add5Years";
        List<VardecStmt> add5YearsParams = new ArrayList<>();
        List<Stmt> add5YearsBody = new ArrayList<>();
        BinaryExp addingYears = new BinaryExp(
            new ThisExp(Optional.of("age")),
            new PlusOp(),
            new IntExp(10)
        );
        add5YearsBody.add(new ReturnStmt(Optional.of(addingYears)));
        methods.add(new MethodDef(add5YearsMethod, add5YearsParams, new IntType(), add5YearsBody));

        return new ClassDef(classname, extend, vardecs, constructor, methods);
        
    }

}
