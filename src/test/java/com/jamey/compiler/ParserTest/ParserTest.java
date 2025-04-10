package com.jamey.compiler.ParserTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.jamey.compiler.Lexer.*;
import com.jamey.compiler.Parser.*;

public class ParserTest {
    @Test
    public void testPrintExp() throws TokenizerException, ParserException{
        Tokenizer tokenizer = new Tokenizer("println ( something )");
        ArrayList<Token> tokens = tokenizer.tokenize();
        System.out.println(tokens);
        Parser parser = new Parser(tokens);
        ParseResult<Exp> p = new ParseResult<Exp>(new PrintlnExp(new VarExp("something")), 0);

        assertEquals(p.result, parser.primaryExp(0).result);
    }
    @Test
    public void testNewExp() throws TokenizerException, ParserException{
        System.out.println("\n\n");
        Tokenizer tokenizer = new Tokenizer("new Classname(true, position, 4)");
        ArrayList<Token> tokens = tokenizer.tokenize();
        System.out.println(tokens);
        Parser parser = new Parser(tokens);
        List<Exp> exps = List.of(
            new BooleanExp(true),
            new VarExp("position"),
            new IntExp(4)
        );
        ParseResult<Exp> p = new ParseResult<Exp>(new NewExp(new ClassType("Classname"), Optional.of(exps)), 0);
        System.out.println("\n\n");
        System.out.println(p.result);
        System.out.println(parser.primaryExp(0).result);
        assertEquals(p.result, parser.primaryExp(0).result);
    }
}
