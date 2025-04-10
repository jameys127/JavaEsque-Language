package com.jamey.compiler.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jamey.compiler.Lexer.*;

public class Parser {
    private final ArrayList<Token> tokens;

    public Parser(final ArrayList<Token> tokens){
        this.tokens = tokens;
    }

    public Token getToken(final int position) throws ParserException {
        if(position >= 0 && position < tokens.size()){
            return tokens.get(position);
        }else{
            throw new ParserException("Out of Tokens");
        }
    }

    public ParseResult<Exp> primaryExp(final int position) throws ParserException{
        final Token token = getToken(position);
        if (token instanceof IdentifierToken id){
            return new ParseResult<Exp>(new VarExp(id.name()), position + 1);
        }else if(token instanceof IntegerToken id){
            return new ParseResult<Exp>(new IntExp(id.value()), position + 1);
        }else if(token instanceof LParenToken){
            final ParseResult<Exp> e = exp(position + 1);
            assertTokenIs(e.position + 1, new RParenToken());
            return new ParseResult<Exp>(e.result, e.position + 2);
        }else if(token instanceof ThisToken){
            return new ParseResult<Exp>(new ThisExp(), position + 1);
        }else if(token instanceof TrueToken){
            return new ParseResult<Exp>(new BooleanExp(true), position + 1);
        }else if(token instanceof FalseToken){
            return new ParseResult<Exp>(new BooleanExp(false), position + 1);
        }else if(token instanceof PrintlnToken){
            assertTokenIs(position + 1, new LParenToken());
            final ParseResult<Exp> e = exp(position + 2);
            PrintlnExp p = new PrintlnExp(e.result);
            assertTokenIs(e.position, new RParenToken());
            return new ParseResult<Exp>(p, e.position + 1);
        }else if(token instanceof NewToken){
            Token other = getToken(position + 1);
            if(other instanceof IdentifierToken id){
                String classname = id.name();
                ClassType classtype = new ClassType(classname);
                assertTokenIs(position + 2, new LParenToken());
                int pos = position + 3;
                if(!(getToken(pos) instanceof RParenToken)){
                    List<Exp> commaExps = new ArrayList<>();
                    ParseResult<Exp> e = exp(pos);
                    commaExps.add(e.result);
                    pos = e.position;
                    while(getToken(pos) instanceof CommaToken){
                        ParseResult<Exp> d = exp(pos + 1);
                        commaExps.add(d.result);
                        pos = d.position;
                    }
                    assertTokenIs(pos, new RParenToken());
                    return new ParseResult<Exp>(new NewExp(classtype, Optional.of(commaExps)), pos + 1);
                }
                return new ParseResult<Exp>(new NewExp(classtype, Optional.empty()), pos + 1);
            }else{
                throw new ParserException("Expected IdentifierToken after statement 'new'; Received: " + other.toString());
            }
        }else{
            throw new ParserException("Expected Expression; Got: " + token);
        }

    }

    public ParseResult<Exp> multExp(final int position) throws ParserException{
        return primaryExp(position);
    }
    public ParseResult<Exp> addExp(final int position) throws ParserException{
        return multExp(position);
    }
    public ParseResult<Exp> exp(final int position)throws ParserException{
        return addExp(position);
    }

    public ParseResult<Type> parseType(final int position) throws ParserException{
        final Token token = getToken(position);
        if(token instanceof IntToken){
            return new ParseResult<Type>(new IntType(), position + 1);
        }else if(token instanceof BooleanToken){
            return new ParseResult<Type>(new BoolType(), position + 1);
        }else if(token instanceof VoidToken){
            return new ParseResult<Type>(new VoidType(), position + 1);
        }else if(token instanceof IdentifierToken id){
            return new ParseResult<Type>(new ClassType(id.name()), position + 1);
        }else {
            throw new ParserException("Expected a DataType; received: "
                                    + tokens.get(position).toString());
        }
    }

    public void assertTokenIs(final int pos, final Token expected) throws ParserException{
        final Token recieved = getToken(pos);
        if(!expected.equals(recieved)){
            throw new ParserException("Expected: " + expected.toString() +
                                      "; Received: " + recieved.toString());
        }
    }
}
