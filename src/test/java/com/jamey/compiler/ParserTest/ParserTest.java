package com.jamey.compiler.ParserTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

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
        ParseResult<Exp> p = new ParseResult<Exp>(new PrintlnExp(new VarExp("something")));

        assertEquals(p.result, parser.primaryExp(0).result);
    }
    
}
