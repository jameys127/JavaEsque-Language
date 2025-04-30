package com.jamey.compiler.TokenizerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.jamey.compiler.Lexer.AddToken;
import com.jamey.compiler.Lexer.ClassToken;
import com.jamey.compiler.Lexer.CommaToken;
import com.jamey.compiler.Lexer.DivisionToken;
import com.jamey.compiler.Lexer.DotToken;
import com.jamey.compiler.Lexer.EqualsToken;
import com.jamey.compiler.Lexer.IdentifierToken;
import com.jamey.compiler.Lexer.InitToken;
import com.jamey.compiler.Lexer.IntegerToken;
import com.jamey.compiler.Lexer.LCurlyBraceToken;
import com.jamey.compiler.Lexer.LParenToken;
import com.jamey.compiler.Lexer.LessOrEqualToken;
import com.jamey.compiler.Lexer.MethodToken;
import com.jamey.compiler.Lexer.MinusToken;
import com.jamey.compiler.Lexer.MultToken;
import com.jamey.compiler.Lexer.PrintlnToken;
import com.jamey.compiler.Lexer.RCurlyBraceToken;
import com.jamey.compiler.Lexer.RParenToken;
import com.jamey.compiler.Lexer.SemicolonToken;
import com.jamey.compiler.Lexer.Token;
import com.jamey.compiler.Lexer.Tokenizer;
import com.jamey.compiler.Lexer.TokenizerException;
import com.jamey.compiler.Lexer.VoidToken;

public class TokenizerTest {
    @Test
    public void testEmpty() throws TokenizerException{
        Tokenizer tokenizer = new Tokenizer("");
        assertEquals(Optional.empty(), tokenizer.tryReadIdentifierOrReservedWordToken());
    }

    @Test
    public void testSkippingWhiteSpace(){
        Tokenizer tokenizer = new Tokenizer(" ");
        tokenizer.skipWhiteSpace();
        assertEquals(1, tokenizer.getPosition());        
    }

    @Test
    public void testReadIntegerToken(){
        Tokenizer tokenizer = new Tokenizer("1");
        assertEquals(Optional.of(new IntegerToken(1)), tokenizer.tryReadIntegerToken());
    }

    @Test
    public void testReadIntegerWithMultipleDigitsToken(){
        Tokenizer tokenizer = new Tokenizer("12");
        assertEquals(Optional.of(new IntegerToken(12)), tokenizer.tryReadIntegerToken());
    }

    @Test
    public void testReadIdentifier(){
        Tokenizer tokenizer = new Tokenizer("wassup");
        assertEquals(Optional.of(new IdentifierToken("wassup")), tokenizer.tryReadIdentifierOrReservedWordToken());
    }
    @Test
    public void testNewBinarySymbols(){
        Tokenizer tokenizer = new Tokenizer("<=");
        assertEquals(Optional.of(new LessOrEqualToken()), tokenizer.tryReadSymbolToken());
    }

    @Test
    public void testReadingSymbolsWithWhiteSpace() throws TokenizerException{
        Tokenizer tokenizer = new Tokenizer("+ - * / = , ; .");
        ArrayList<Token> tokens = new ArrayList<>(List.of(new AddToken(),
                                                        new MinusToken(),
                                                        new MultToken(),
                                                        new DivisionToken(),
                                                        new EqualsToken(),
                                                        new CommaToken(),
                                                        new SemicolonToken(),
                                                        new DotToken()));
        assertEquals(tokens, tokenizer.tokenize());
    }
    @Test
    public void testReadCodeSnippet() throws TokenizerException{
        Tokenizer tokenizer = new Tokenizer("class Car { init(){} method vroom() void { println(2); } }");
        ArrayList<Token> tokens = new ArrayList<>(List.of(new ClassToken(),
                                                        new IdentifierToken("Car"),
                                                        new LCurlyBraceToken(),
                                                        new InitToken(),
                                                        new LParenToken(),
                                                        new RParenToken(),
                                                        new LCurlyBraceToken(),
                                                        new RCurlyBraceToken(),
                                                        new MethodToken(),
                                                        new IdentifierToken("vroom"),
                                                        new LParenToken(),
                                                        new RParenToken(),
                                                        new VoidToken(),
                                                        new LCurlyBraceToken(),
                                                        new PrintlnToken(),
                                                        new LParenToken(),
                                                        new IntegerToken(2),
                                                        new RParenToken(),
                                                        new SemicolonToken(),
                                                        new RCurlyBraceToken(),
                                                        new RCurlyBraceToken()));
        assertEquals(tokens, tokenizer.tokenize());
    }

    @Test
    public void testExceptions(){
        Tokenizer tokenizer = new Tokenizer("if(true){ return 1;} else { [] }");
        assertThrows(TokenizerException.class, () -> { tokenizer.tokenize(); });
    }
}
