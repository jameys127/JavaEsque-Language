package com.jamey.compiler.Lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Tokenizer {
    public final String input;
    private int position;

    public Tokenizer(final String input){
        this.input = input;
        position = 0;
    }

    public int getPosition(){
        return position;
    }

    public void skipWhiteSpace(){
        while(position < input.length() && Character.isWhitespace(input.charAt(position))){
                position++;
        }
    }

    public Optional<Token> tryReadIntegerToken(){
        String digits = "";
        while (position < input.length() && Character.isDigit(input.charAt(position))){
            digits += input.charAt(position);
            position++;
        }
        if(digits.length() == 0){
            return Optional.empty();
        }else{
            return Optional.of(new IntegerToken(Integer.parseInt(digits)));
        }
    }//tryReadIntegerToken()

    public Optional<List<Token>> tryReadStringLiteral() throws TokenizerException{
        if(position < input.length() && input.charAt(position) == '"'){
            position++;
            String chars = "";
            while(position < input.length() && input.charAt(position) != '"'){
                chars += input.charAt(position);
                position++;
            }
            if(position >= input.length()){
                throw new TokenizerException("Never closed the quotation marks");
            }
            position++;
            
            List<Token> listStringLiteral = new ArrayList<>();
            listStringLiteral.add(new QuoteToken());
            listStringLiteral.add(new IdentifierToken(chars));
            listStringLiteral.add(new QuoteToken());
            return Optional.of(listStringLiteral);
        }else{
            return Optional.empty();
        }
    }

    public Optional<Token> tryReadIdentifierOrReservedWordToken(){
        if(position < input.length() && Character.isLetter(input.charAt(position))){
            String chars = "" + input.charAt(position);
            position++;
            while(position < input.length() && Character.isLetterOrDigit(input.charAt(position))){
                chars += input.charAt(position);
                position++;
            }
            if(chars.equals("println")){
                return Optional.of(new PrintlnToken());
            }else if(chars.equals("boolean")){
                return Optional.of(new BooleanToken());
            }else if(chars.equals("break")){
                return Optional.of(new BreakToken());
            }else if(chars.equals("class")){
                return Optional.of(new ClassToken());
            }else if(chars.equals("if")){
                return Optional.of(new IfToken());
            }else if(chars.equals("else")){
                return Optional.of(new ElseToken());
            }else if(chars.equals("extends")){
                return Optional.of(new ExtendsToken());
            }else if(chars.equals("true")){
                return Optional.of(new TrueToken());
            }else if(chars.equals("false")){
                return Optional.of(new FalseToken());
            }else if(chars.equals("init")){
                return Optional.of(new InitToken());
            }else if(chars.equals("int")){
                return Optional.of(new IntToken());
            }else if(chars.equals("method")){
                return Optional.of(new MethodToken());
            }else if(chars.equals("new")){
                return Optional.of(new NewToken());
            }else if(chars.equals("return")){
                return Optional.of(new ReturnToken());
            }else if(chars.equals("String")){
                return Optional.of(new StringToken());
            }else if(chars.equals("super")){
                return Optional.of(new SuperToken());
            }else if(chars.equals("this")){
                return Optional.of(new ThisToken());
            }else if(chars.equals("void")){
                return Optional.of(new VoidToken());
            }else if(chars.equals("while")){
                return Optional.of(new WhileToken());
            }else{
                return Optional.of(new IdentifierToken(chars));
            }
            
        } else {
            return Optional.empty();
        }
    }//tryReadIdentifierOrReservedWordToken()

    public Optional<Token> tryReadSymbolToken(){
        if(input.startsWith("+", position)){
            position++;
            return Optional.of(new AddToken());
        }else if(input.startsWith(",", position)){
            position++;
            return Optional.of(new CommaToken());
        }else if(input.startsWith("/", position)){
            position++;
            return Optional.of(new DivisionToken());
        }else if(input.startsWith(".", position)){
            position++;
            return Optional.of(new DotToken());
        }else if(input.startsWith("=", position)){
            position++;
            return Optional.of(new EqualsToken());
        }else if(input.startsWith("{", position)){
            position++;
            return Optional.of(new LCurlyBraceToken());
        }else if(input.startsWith("(", position)){
            position++;
            return Optional.of(new LParenToken());
        }else if(input.startsWith("-", position)){
            position++;
            return Optional.of(new MinusToken());
        }else if(input.startsWith("*", position)){
            position++;
            return Optional.of(new MultToken());
        }else if(input.startsWith("}", position)){
            position++;
            return Optional.of(new RCurlyBraceToken());
        }else if(input.startsWith(")", position)){
            position++;
            return Optional.of(new RParenToken());
        }else if(input.startsWith(";", position)){
            position++;
            return Optional.of(new SemicolonToken());
        }else if(input.startsWith("\"", position)){
            position++;
            return Optional.of(new QuoteToken());
        }
        else {
            return Optional.empty();
        }
    }//tryReadSymbolToken()

    public List<Token> readToken() throws TokenizerException {
        Optional<List<Token>> listOfTokens;
        Optional<Token> token;
        if((listOfTokens = tryReadStringLiteral()).isPresent()){
            return listOfTokens.get();
        }
        if((token = tryReadIdentifierOrReservedWordToken()).isPresent() ||
            (token = tryReadIntegerToken()).isPresent() ||
            (token = tryReadSymbolToken()).isPresent()){
                List<Token> list = new ArrayList<>();
                list.add(token.get());
                return list;
        }else {
                throw new TokenizerException("Character not recognized: " + input.charAt(position));
        }
    }

    public ArrayList<Token> tokenize() throws TokenizerException{
        final ArrayList<Token> tokens = new ArrayList<Token>();
        skipWhiteSpace();
        while (position < input.length()){
            List<Token> list = readToken();
            for(Token t : list){
                tokens.add(t);
            }
            skipWhiteSpace();
        }
        return tokens;
    }


}
