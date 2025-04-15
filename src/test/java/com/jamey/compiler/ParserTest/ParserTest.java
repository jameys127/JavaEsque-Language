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
        // Test an if statement with both then and else blocks
        Tokenizer tokenizer = new Tokenizer("if (x) { y = 5; } else { z = 10; }");
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        
        // Build the expected AST
        VarExp conditionExp = new VarExp("x");
        
        List<Stmt> thenBlock = new ArrayList<>();
        thenBlock.add(new AssignStmt("y", new IntExp(5)));
        
        List<Stmt> elseBlock = new ArrayList<>();
        elseBlock.add(new AssignStmt("z", new IntExp(10)));
        
        IfStmt expectedIfStmt = new IfStmt(conditionExp, thenBlock, Optional.of(elseBlock));
        
        // Parse and compare
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
        // Test a constructor with parameters and a super call
        Tokenizer tokenizer = new Tokenizer("init() { super(y, true); d = e + 1; println(d); }");
        ArrayList<Token> tokens = tokenizer.tokenize();
        Parser parser = new Parser(tokens);
        
        // Build the expected AST
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
        
        // Parse and compare
        ParseResult<Constructor> result = parser.parseConstructor(0);
        assertEquals(expectedConstructor, result.result);
        assertEquals(tokens.size(), result.position);
    }
}
