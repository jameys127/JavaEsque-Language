package com.jamey.compiler.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.jamey.compiler.Lexer.*;
import com.jamey.compiler.Parser.MethodCallExp.MethodCall;

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

    public ParseResult<List<VardecStmt>> parseCommaVardec(final int position) throws ParserException{
        List<VardecStmt> list = new ArrayList<>();
        if(getToken(position) instanceof RParenToken){
            return new ParseResult<List<VardecStmt>>(list, position + 1);
        }
        ParseResult<Type> type = parseType(position);
        if(getToken(type.position) instanceof IdentifierToken id){
            String name = id.name();
            list.add(new VardecStmt(type.result, name));
            int pos = type.position + 1;
            while(getToken(pos) instanceof CommaToken){
                type = parseType(pos + 1);
                if(getToken(type.position) instanceof IdentifierToken other){
                    name = other.name();
                    list.add(new VardecStmt(type.result, name));
                    pos = type.position + 1;
                }else{
                    throw new ParserException("Expected an Identifier after the type in a variable declaration");
                }
            }
            assertTokenIs(pos, new RParenToken());
            return new ParseResult<List<VardecStmt>>(list, pos + 1);
        }else{
            throw new ParserException("Expected an Identifier after the type in a variable declaration");
        }
    }

    public ParseResult<List<Exp>> parseCommaExp(final int position)throws ParserException{
        List<Exp> list = new ArrayList<>();
        if(getToken(position) instanceof RParenToken){
            return new ParseResult<List<Exp>>(list, position + 1);
        }
        ParseResult<Exp> exp = exp(position);
        list.add(exp.result);
        int pos = exp.position;
        while(getToken(pos) instanceof CommaToken){
            exp = exp(pos + 1);
            list.add(exp.result);
            pos = exp.position;
        }
        assertTokenIs(pos, new RParenToken());
        return new ParseResult<List<Exp>>(list, pos + 1);
    }

    public ParseResult<Exp> primaryExp(final int position) throws ParserException{
        final Token token = getToken(position);
        if(token instanceof IntegerToken id){
            return new ParseResult<Exp>(new IntExp(id.value()), position + 1);
        }else if(token instanceof LParenToken){
            final ParseResult<Exp> e = exp(position + 1);
            assertTokenIs(e.position + 1, new RParenToken());
            return new ParseResult<Exp>(e.result, e.position + 2);
        }else if(token instanceof QuoteToken){
            ParseResult<Exp> e = exp(position + 1);
            if(!(e.result instanceof VarExp)){
                throw new ParserException("Expected valid String inside quotations");
            }else{
                VarExp stringliteral = (VarExp)e.result;
                assertTokenIs(e.position, new QuoteToken());
                return new ParseResult<Exp>(new StrExp(stringliteral.name()), e.position + 1);
            }
        }else if(token instanceof ThisToken){
            if(getToken(position + 1) instanceof DotToken){
                if(getToken(position + 2) instanceof IdentifierToken id){
                    String name = id.name();
                    return new ParseResult<Exp>(new ThisExp(Optional.of(name)), position + 3);
                }else{
                    throw new ParserException("Expected Identifier after 'this' call");
                }
            }
            return new ParseResult<Exp>(new ThisExp(Optional.empty()), position + 1);
        }else if(token instanceof TrueToken){
            return new ParseResult<Exp>(new BooleanExp(true), position + 1);
        }else if(token instanceof FalseToken){
            return new ParseResult<Exp>(new BooleanExp(false), position + 1);
        }else if(token instanceof PrintlnToken){
            assertTokenIs(position + 1, new LParenToken());
            final ParseResult<Exp> e = exp(position + 2);
            assertTokenIs(e.position, new RParenToken());
            return new ParseResult<Exp>(new PrintlnExp(e.result), e.position + 1);
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
        }else if (token instanceof IdentifierToken id){
            return new ParseResult<Exp>(new VarExp(id.name()), position + 1);
        }else{
            throw new ParserException("Expected Expression; Got: " + token);
        }

    }
    public ParseResult<Exp> callExp(final int position) throws ParserException{
        ParseResult<Exp> e = primaryExp(position);
        if(e.position < tokens.size() && getToken(e.position).equals(new DotToken())){
            int pos = e.position + 1;
            List<MethodCall> methods = new ArrayList<>(); 
            boolean shouldrun = true;
            while(shouldrun){
                List<Exp> exps = new ArrayList<>();
                Token other = getToken(pos);
                pos++;
                if(other instanceof IdentifierToken id){
                    assertTokenIs(pos, new LParenToken());
                    pos++;
                    if(!(getToken(pos) instanceof RParenToken)){
                        ParseResult<Exp> d = exp(pos);
                        exps.add(d.result);
                        pos = d.position;
                        while(getToken(pos) instanceof CommaToken){
                            ParseResult<Exp> f = exp(pos + 1);
                            exps.add(f.result);
                            pos = f.position;
                        }
                        assertTokenIs(pos, new RParenToken());
                    }
                    pos++;
                    MethodCall dummyMethod = new MethodCall(id.name(), exps);
                    methods.add(dummyMethod);
                }else{
                    throw new ParserException("Expected Identifier Token; Received: " + other.toString());
                }
                try{
                    Token maybeDot = getToken(pos);
                    if(maybeDot.equals(new DotToken())){
                        pos++;
                    }else{
                        shouldrun = false;
                    }
                }catch (ParserException error){
                    shouldrun = false;
                }
            }
            return new ParseResult<Exp>(new MethodCallExp(e.result, methods), pos);
        }
        return new ParseResult<Exp>(e.result, e.position);
    }


    public ParseResult<Exp> multExp(final int position) throws ParserException{
        ParseResult<Exp> e = callExp(position);
        Exp result = e.result;
        int pos = e.position;
        while(pos < tokens.size()){
            final Token t = getToken(pos);
            if(t instanceof MultToken || t instanceof DivisionToken){
                final Op op = (t instanceof MultToken) ? new MultOp() : new DivOp();
                final ParseResult<Exp> e2 = callExp(pos + 1);
                result = new BinaryExp(result, op, e2.result);
                pos = e2.position;
            }else{
                break;
            }
        }
        return new ParseResult<Exp>(result, pos);
    }


    public ParseResult<Exp> addExp(final int position) throws ParserException{
        ParseResult<Exp> e = multExp(position);
        Exp result = e.result;
        int pos = e.position;
        while(pos < tokens.size()){
            final Token t = getToken(pos);
            if(t instanceof AddToken || t instanceof MinusToken){
                final Op op = (t instanceof AddToken) ? new PlusOp() : new MinusOp();
                final ParseResult<Exp> e2 = multExp(pos + 1);
                result = new BinaryExp(result, op, e2.result);
                pos = e2.position;
            }else{
                break;
            }
        }
        return new ParseResult<Exp>(result, pos);
    }


    public ParseResult<Exp> exp(final int position)throws ParserException{
        return addExp(position);
    }

    public ParseResult<Stmt> stmt(final int position)throws ParserException{
        Token token = getToken(position);
        if(token instanceof IfToken){
            List<Stmt> stmtList = new ArrayList<>();
            assertTokenIs(position + 1, new LParenToken());
            ParseResult<Exp> e = exp(position + 2);
            assertTokenIs(e.position, new RParenToken());
            assertTokenIs(e.position + 1, new LCurlyBraceToken());
            int pos = e.position + 2;
            while(true){
                ParseResult<Stmt> stmt = stmt(pos);
                stmtList.add(stmt.result);
                pos = stmt.position;
                if(getToken(pos) instanceof RCurlyBraceToken){
                    break;
                }
            }
            if(pos + 1 < tokens.size() && getToken(pos + 1) instanceof ElseToken){
                List<Stmt> list = new ArrayList<>();
                assertTokenIs(pos + 2, new LCurlyBraceToken());
                pos += 3;
                while(!(getToken(pos) instanceof RCurlyBraceToken)){
                    ParseResult<Stmt> other = stmt(pos);
                    list.add(other.result);
                    pos = other.position;    
                }
                return new ParseResult<Stmt>(new IfStmt(e.result, stmtList, Optional.of(list)), pos + 1);
            }
            return new ParseResult<Stmt>(new IfStmt(e.result, stmtList, Optional.empty()) , pos + 1);
        }else if(token instanceof WhileToken){
            assertTokenIs(position + 1, new LParenToken());
            ParseResult<Exp> condition = exp(position + 2);
            assertTokenIs(condition.position, new RParenToken());
            assertTokenIs(condition.position + 1, new LCurlyBraceToken());
            int pos = condition.position + 2;
            List<Stmt> block = new ArrayList<>();
            while(true){
                ParseResult<Stmt> body = stmt(pos);
                block.add(body.result);
                pos = body.position;
                if(getToken(pos) instanceof RCurlyBraceToken){
                    break;
                }
            }
            return new ParseResult<Stmt>(new WhileStmt(condition.result, block), pos + 1);
        }else if(token instanceof BreakToken){
            assertTokenIs(position + 1, new SemicolonToken());
            return new ParseResult<Stmt>(new BreakStmt(), position + 2);
        }else if(token instanceof ReturnToken){
            if(!(getToken(position + 1) instanceof SemicolonToken)){
                ParseResult<Exp> exp = exp(position + 1);
                assertTokenIs(exp.position, new SemicolonToken());
                return new ParseResult<Stmt>(new ReturnStmt(Optional.of(exp.result)), exp.position + 1);
            }
            return new ParseResult<Stmt>(new ReturnStmt(Optional.empty()), position + 2);
        }else{
            try{
                //Vardec Stmt specifically
                ParseResult<Type> e = parseType(position);
                if(getToken(e.position) instanceof IdentifierToken id){
                    assertTokenIs(e.position + 1, new SemicolonToken());
                    return new ParseResult<Stmt>(new VardecStmt(e.result, id.name()), e.position + 2);
                }else{
                    throw new ParserException("Expected a Variable Identifier; Received: " + getToken(e.position));
                }
            }catch (ParserException error){
                //nothin
            }
            try{
                //Assignemnt Stmt specifically with a regular identifier variable
                if(token instanceof IdentifierToken id && getToken(position + 1) instanceof EqualsToken){
                    ParseResult<Exp> d = exp(position + 2);
                    assertTokenIs(d.position, new SemicolonToken());
                    return new ParseResult<Stmt>(new AssignStmt(Optional.empty(), id.name(), d.result), d.position + 1);
                }
            }catch(ParserException error){
                //more nothin
            }try{
                //Assignemnt Stmt specifically with a this.variablename identifier
                if(token instanceof ThisToken && getToken(position + 3) instanceof EqualsToken){
                    ParseResult<Exp> exp = exp(position);
                    ThisExp thisexp = (ThisExp)exp.result;
                    ParseResult<Exp> assignExp = exp(exp.position + 1);
                    assertTokenIs(assignExp.position, new SemicolonToken());
                    return new ParseResult<Stmt>(new AssignStmt(Optional.of(thisexp), thisexp.parentVar().get(), assignExp.result), assignExp.position + 1);
                }
            }catch(ParserException error){
                //lots of nothin
            }
            try{
                // And finally the ExpStmt
                ParseResult<Exp> e = exp(position);
                assertTokenIs(e.position, new SemicolonToken());
                return new ParseResult<Stmt>(new ExpStmt(e.result), e.position + 1);
            }catch(ParserException error){
                throw new ParserException("Not a valid Statement: " + error);
            }
        }
    }

    public ParseResult<MethodDef> parseMethodDef(final int position) throws ParserException{
        assertTokenIs(position, new MethodToken());
        Token token = getToken(position + 1);
        if(token instanceof IdentifierToken id){

            String name = id.name();
            assertTokenIs(position + 2, new LParenToken());
            int pos = position + 3;
            List<VardecStmt> vardecStmts = new ArrayList<>();
            ParseResult<List<VardecStmt>> list = parseCommaVardec(pos);

            vardecStmts = list.result;
            pos = list.position;
            ParseResult<Type> type = parseType(pos);
            assertTokenIs(type.position, new LCurlyBraceToken());
            pos = type.position + 1;
            List<Stmt> stmtList = new ArrayList<>();
            while(!(getToken(pos) instanceof RCurlyBraceToken)){
                ParseResult<Stmt> stmt = stmt(pos);
                stmtList.add(stmt.result);
                pos = stmt.position;
            }
            return new ParseResult<MethodDef>(new MethodDef(name, vardecStmts, type.result, stmtList), pos + 1);
        }else{
            throw new ParserException("Expected method Identifier Token after 'method'; Received: " + token.toString());
        }
    }

    public ParseResult<Constructor> parseConstructor(final int position) throws ParserException{
        List<VardecStmt> vardecStmts = new ArrayList<>();
        List<Exp> expList = new ArrayList<>();
        List<Stmt> stmtList = new ArrayList<>();
        
        assertTokenIs(position, new InitToken());
        assertTokenIs(position + 1, new LParenToken());
        ParseResult<List<VardecStmt>> vardecs = parseCommaVardec(position + 2);
        vardecStmts = vardecs.result;
        int pos = vardecs.position;

        assertTokenIs(pos, new LCurlyBraceToken());
        if(getToken(pos + 1) instanceof SuperToken){
            assertTokenIs(pos + 2, new LParenToken());
            ParseResult<List<Exp>> exps = parseCommaExp(pos + 3);
            expList = exps.result;
            assertTokenIs(exps.position, new SemicolonToken());
            pos = exps.position + 1;
            while(!(getToken(pos) instanceof RCurlyBraceToken)){
                ParseResult<Stmt> stmt = stmt(pos);
                stmtList.add(stmt.result);
                pos = stmt.position;
            }
            return new ParseResult<Constructor>(new Constructor(vardecStmts, Optional.of(expList), stmtList), pos + 1);
        }
        pos++;
        while(!(getToken(pos) instanceof RCurlyBraceToken)){
            ParseResult<Stmt> stmt = stmt(pos);
            stmtList.add(stmt.result);
            pos = stmt.position;
        }
        return new ParseResult<Constructor>(new Constructor(vardecStmts, Optional.empty(), stmtList), pos + 1);
    }

    public ParseResult<ClassDef> parseClassDef(final int position) throws ParserException{
        String name;
        Optional<String> extend = Optional.empty();
        List<VardecStmt> vardecList = new ArrayList<>();
        List<MethodDef> methodList = new ArrayList<>();
        
        assertTokenIs(position, new ClassToken());
        if(getToken(position + 1) instanceof IdentifierToken id){
            int pos = position + 2;
            name = id.name();
            if(getToken(pos) instanceof ExtendsToken){
                if(getToken(pos + 1) instanceof IdentifierToken t){
                    extend = Optional.of(t.name());
                    pos += 2;
                }
            }
            assertTokenIs(pos, new LCurlyBraceToken());
            pos++;
            while(!(getToken(pos) instanceof InitToken)){
                try{
                    ParseResult<Stmt> stmt = stmt(pos);
                    VardecStmt x = (VardecStmt)stmt.result;
                    vardecList.add(x);
                    pos = stmt.position;
                } catch(ParserException e){
                    throw new ParserException("Statement is not a Variable Decleration; " + e.getMessage());
                }
            }
            ParseResult<Constructor> constructor = parseConstructor(pos);
            pos = constructor.position;
            while(!(getToken(pos) instanceof RCurlyBraceToken)){
                ParseResult<MethodDef> method = parseMethodDef(pos);
                methodList.add(method.result);
                pos = method.position;
            }
            return new ParseResult<ClassDef>(new ClassDef(name, extend, vardecList, constructor.result, methodList), pos + 1);
        }else{
            throw new ParserException("Expected an Identifier for the classname; Received: " + getToken(position + 1).toString());
        }
    }

    public ParseResult<Program> parseProgram(final int position) throws ParserException{
        List<ClassDef> classdefs = new ArrayList<>();
        List<Stmt> stmts = new ArrayList<>();
        if(position >= tokens.size()){
            return new ParseResult<Program>(new Program(classdefs, stmts), position);
        }
        int pos = position;
        // Try to parse classes first
        while(pos < tokens.size()) {
            try {
                if(getToken(pos) instanceof ClassToken) {
                    ParseResult<ClassDef> classResult = parseClassDef(pos);
                    classdefs.add(classResult.result);
                    pos = classResult.position;
                } else {
                    break;
                }
            } catch (ParserException e) {
                break;
            }
        }
        
        // Now parse statements
        while(pos < tokens.size()) {
            try {
                ParseResult<Stmt> stmtResult = stmt(pos);
                stmts.add(stmtResult.result);
                pos = stmtResult.position;
            } catch (ParserException e) {
                throw new ParserException("Invalid statement at position " + pos + ": " + e.getMessage());
            }
        }

        if(stmts.isEmpty()){
            throw new ParserException("There needs to be atleast one statement in the program");
        }
        return new ParseResult<Program>(new Program(classdefs, stmts), pos);
    }

    public Program parseWholeProgram() throws ParserException{
        final ParseResult<Program> p = parseProgram(0);
        if(p.position ==tokens.size()){
            return p.result;
        } else {
            throw new ParserException("Invalid token at position: " + p.position);
        }
    }

    public ParseResult<Type> parseType(final int position) throws ParserException{
        final Token token = getToken(position);
        if(token instanceof IntToken){
            return new ParseResult<Type>(new IntType(), position + 1);
        }else if(token instanceof BooleanToken){
            return new ParseResult<Type>(new BoolType(), position + 1);
        }else if(token instanceof VoidToken){
            return new ParseResult<Type>(new VoidType(), position + 1);
        }else if(token instanceof StringToken){
            return new ParseResult<Type>(new StringType(), position + 1);
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
