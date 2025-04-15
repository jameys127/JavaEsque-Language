package com.jamey.compiler.ParserTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.jamey.compiler.Lexer.*;
import com.jamey.compiler.Parser.*;
import com.jamey.compiler.Parser.MethodCallExp.MethodCall;

public class ParserTest {
    @Test
    public void testPrintExp() throws TokenizerException, ParserException{
        Tokenizer tokenizer = new Tokenizer("println ( something )");
        ArrayList<Token> tokens = tokenizer.tokenize();
        // System.out.println(tokens);
        Parser parser = new Parser(tokens);
        ParseResult<Exp> p = new ParseResult<Exp>(new PrintlnExp(new VarExp("something")), 0);

        assertEquals(p.result, parser.exp(0).result);
    }
    @Test
    public void testNewExp() throws TokenizerException, ParserException{
        // System.out.println("\n\n");
        Tokenizer tokenizer = new Tokenizer("new Classname(true, position, 4)");
        ArrayList<Token> tokens = tokenizer.tokenize();
        // System.out.println(tokens);
        Parser parser = new Parser(tokens);
        List<Exp> exps = List.of(
            new BooleanExp(true),
            new VarExp("position"),
            new IntExp(4)
        );
        ParseResult<Exp> p = new ParseResult<Exp>(new NewExp(new ClassType("Classname"), Optional.of(exps)), 0);
        // System.out.println("\n\n");
        // System.out.println(p.result);
        // System.out.println(parser.exp(0).result);
        assertEquals(p.result, parser.exp(0).result);
    }

    @Test
    public void testCallExp() throws ParserException, TokenizerException{
        System.out.println("\n\n");
        Tokenizer tokenizer = new Tokenizer("Obj1.methodname()");
        ArrayList<Token> tokens = tokenizer.tokenize();
        System.out.println(tokens);
        Parser parser = new Parser(tokens);


        Exp obj = new VarExp("Obj1");
        List<Exp> stuff = new ArrayList<>();
        MethodCall method = new MethodCall("methodname", stuff);
        List<MethodCall> methods = new ArrayList<>();
        methods.add(method);
        ParseResult<Exp> exp = new ParseResult<Exp>(new MethodCallExp(obj, methods), 6);

        assertEquals(exp.result, parser.exp(0).result);
    }

    @Test
    public void testCallWithMultExp() throws ParserException, TokenizerException{
        System.out.println("\n\n");
        Tokenizer tokenizer = new Tokenizer("Obj1.methodname().size(2)");
        ArrayList<Token> tokens = tokenizer.tokenize();
        System.out.println(tokens);
        Parser parser = new Parser(tokens);


        Exp obj = new VarExp("Obj1");

        List<Exp> two = new ArrayList<>();
        IntExp littletwo = new IntExp(2);
        two.add(littletwo);
        MethodCall method2 = new MethodCall("size", two);

        List<Exp> stuff = new ArrayList<>();
        MethodCall method = new MethodCall("methodname", stuff);


        List<MethodCall> methods = new ArrayList<>();
        methods.add(method);
        methods.add(method2);

        ParseResult<Exp> exp = new ParseResult<Exp>(new MethodCallExp(obj, methods), 11);

        assertEquals(exp.result, parser.exp(0).result);
    }
    @Test
    public void testPrintlnStmt() throws TokenizerException, ParserException{
        Tokenizer tokenizer = new Tokenizer("println(x);");
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);

        ParseResult<Stmt> stmt = new ParseResult<Stmt>(new ExpStmt(new PrintlnExp(new VarExp("x"))), 0);
        ParseResult<Stmt> parsed = parser.stmt(0);

        assertEquals(stmt.result, parsed.result);
    }

    @Test
    public void testIfStatement() throws TokenizerException, ParserException {
        Tokenizer tokenizer = new Tokenizer("if (x) { y = 5; } else { z = 10; }");
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        
        VarExp conditionExp = new VarExp("x");
        
        List<Stmt> thenBlock = new ArrayList<>();
        thenBlock.add(new AssignStmt("y", new IntExp(5)));
        
        List<Stmt> elseBlock = new ArrayList<>();
        elseBlock.add(new AssignStmt("z", new IntExp(10)));
        
        IfStmt expectedIfStmt = new IfStmt(conditionExp, thenBlock, Optional.of(elseBlock));
        
        ParseResult<Stmt> result = parser.stmt(0);
        assertEquals(expectedIfStmt, result.result);
        assertEquals(tokens.size(), result.position);
    }

    @Test
    public void testMethodDef()throws TokenizerException, ParserException{
        Tokenizer tokenizer = new Tokenizer("method stuff(int x, int y) void {x = y + 2; println(x);}");
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        System.out.println("\n\n\n"+tokens.toString()+"\n\n\n");

        String name = "stuff";

        List<VardecStmt> vardecs = new ArrayList<>();
        VardecStmt vardec1 = new VardecStmt(new IntType(), "x");
        VardecStmt vardec2 = new VardecStmt(new IntType(), "y");
        vardecs.add(vardec1);
        vardecs.add(vardec2);

        Type type = new VoidType();

        List<Stmt> stmts = new ArrayList<>();
        Op op = new PlusOp();
        Exp i = new VarExp("y");
        Exp j = new IntExp(2);
        Exp exp = new BinaryExp(i, op, j);
        AssignStmt assignment = new AssignStmt("x", exp);
        stmts.add(assignment);

        ExpStmt printstmt = new ExpStmt(new PrintlnExp(new VarExp("x")));
        stmts.add(printstmt);

        MethodDef method = new MethodDef(name, vardecs, type, stmts);

        ParseResult<MethodDef> result = parser.parseMethodDef(0);

        assertEquals(method, result.result);
    }

    @Test
    public void testConstructorWithSuper() throws TokenizerException, ParserException {
        Tokenizer tokenizer = new Tokenizer("init() { super(y, true); d = e + 1; println(d); }");
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        
        List<VardecStmt> params = new ArrayList<>();
        
        List<Exp> superArgs = new ArrayList<>();
        superArgs.add(new VarExp("y"));
        superArgs.add(new BooleanExp(true));
        
        List<Stmt> body = new ArrayList<>();
        body.add(new AssignStmt("d", 
            new BinaryExp(
                new VarExp("e"),
                new PlusOp(),
                new IntExp(1)
            )
        ));
        body.add(new ExpStmt(new PrintlnExp(new VarExp("d"))));
        
        Constructor expectedConstructor = new Constructor(params, Optional.of(superArgs), body);
        
        ParseResult<Constructor> result = parser.parseConstructor(0);
        assertEquals(expectedConstructor, result.result);
        assertEquals(tokens.size(), result.position);
    }

    @Test
    public void testClassDef() throws TokenizerException, ParserException{
        String input = """
                class Something{
                    int y;
                    init(){}
                    method stuff() void {println(x);}
                    method otherstuff() void {x = 2 + 2;}
                }
                """;
        Tokenizer tokenizer = new Tokenizer(input);
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        ParseResult<ClassDef> result = parser.parseClassDef(0);

        List<VardecStmt> vardecs = new ArrayList<>();
        VardecStmt vardec = new VardecStmt(new IntType(), "y");
        vardecs.add(vardec);

        List<VardecStmt> vardeclist = new ArrayList<>();
        List<Stmt> stmtlist = new ArrayList<>();
        Constructor constructor = new Constructor(vardeclist, Optional.empty(), stmtlist);

        List<MethodDef> methods = new ArrayList<>();
        List<Stmt> methodStmts = new ArrayList<>();
        Stmt printstmt = new ExpStmt(new PrintlnExp(new VarExp("x")));
        methodStmts.add(printstmt);
        MethodDef method = new MethodDef("stuff", vardeclist, new VoidType(), methodStmts);
        methods.add(method);

        Stmt assignStmt = new AssignStmt("x", new BinaryExp(new IntExp(2), new PlusOp(), new IntExp(2)));
        List<Stmt> othermethodstmts = new ArrayList<>();
        othermethodstmts.add(assignStmt);
        MethodDef othermethod = new MethodDef("otherstuff", vardeclist, new VoidType(), othermethodstmts);
        methods.add(othermethod);

        ClassDef expected = new ClassDef("Something", Optional.empty(), vardecs, constructor, methods);

        assertEquals(expected, result.result);
        
    }

    @Test
public void testParseWholeProgram() throws TokenizerException, ParserException {
    String input = """
            class Animal {
              init() {}
              method speak() void { return println(0); }
            }
            class Cat extends Animal {
              init() { super(); }
              method speak() void { return println(1); }
            }
            Animal cat;
            cat = new Cat();
            cat.speak();
            """;
    
    Tokenizer tokenizer = new Tokenizer(input);
    ArrayList<Token> tokens = tokenizer.tokenize();
    Parser parser = new Parser(tokens);
    Program result = parser.parseWholeProgram();
    
    // Build expected program structure
    List<ClassDef> expectedClasses = new ArrayList<>();
    
    // Animal class
    List<VardecStmt> animalVardecs = new ArrayList<>();
    List<VardecStmt> emptyParams = new ArrayList<>();
    List<Stmt> emptyStmts = new ArrayList<>();
    Constructor animalConstructor = new Constructor(emptyParams, Optional.empty(), emptyStmts);
    
    List<Stmt> speakStmts = new ArrayList<>();
    speakStmts.add(new ReturnStmt(Optional.of(new PrintlnExp(new IntExp(0)))));
    MethodDef speakMethod = new MethodDef("speak", emptyParams, new VoidType(), speakStmts);
    
    List<MethodDef> animalMethods = new ArrayList<>();
    animalMethods.add(speakMethod);
    
    ClassDef animalClass = new ClassDef("Animal", Optional.empty(), animalVardecs, animalConstructor, animalMethods);
    expectedClasses.add(animalClass);
    
    // Cat class
    List<VardecStmt> catVardecs = new ArrayList<>();
    List<Exp> superArgs = new ArrayList<>();
    List<Stmt> catConstructorStmts = new ArrayList<>();
    Constructor catConstructor = new Constructor(emptyParams, Optional.of(superArgs), catConstructorStmts);
    
    List<Stmt> catSpeakStmts = new ArrayList<>();
    catSpeakStmts.add(new ReturnStmt(Optional.of(new PrintlnExp(new IntExp(1)))));
    MethodDef catSpeakMethod = new MethodDef("speak", emptyParams, new VoidType(), catSpeakStmts);
    
    List<MethodDef> catMethods = new ArrayList<>();
    catMethods.add(catSpeakMethod);
    
    ClassDef catClass = new ClassDef("Cat", Optional.of("Animal"), catVardecs, catConstructor, catMethods);
    expectedClasses.add(catClass);
    
    // Program statements
    List<Stmt> expectedStmts = new ArrayList<>();
    expectedStmts.add(new VardecStmt(new ClassType("Animal"), "cat"));
    expectedStmts.add(new AssignStmt("cat", new NewExp(new ClassType("Cat"), Optional.empty())));
    
    List<MethodCall> methodCalls = new ArrayList<>();
    methodCalls.add(new MethodCall("speak", new ArrayList<>()));
    expectedStmts.add(new ExpStmt(new MethodCallExp(new VarExp("cat"), methodCalls)));
    
    Program expected = new Program(expectedClasses, expectedStmts);
    
    assertEquals(expected, result);
}
    


}
