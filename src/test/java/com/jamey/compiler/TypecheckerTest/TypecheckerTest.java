package com.jamey.compiler.TypecheckerTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.jamey.compiler.Typechecker.*;
import com.jamey.compiler.Parser.*;

public class TypecheckerTest {
    public static final Map<Variable, Type> EMPTY_TYPE_ENV = new HashMap<Variable, Type>();
    @Test
    public void typecheckNum() throws TypecheckerErrorException{
        assertEquals(new IntType(), Typechecker.typecheckExp(new IntExp(7), EMPTY_TYPE_ENV));
    }

    @Test
    public void typecheckBool() throws TypecheckerErrorException{
        assertEquals(new BoolType(), Typechecker.typecheckExp(new BooleanExp(true), EMPTY_TYPE_ENV));
    }

    @Test
    public void typecheckVardecAndAssign() throws TypecheckerErrorException{
        Stmt stmt = new VardecStmt(new IntType(), "x");
        Stmt assign = new AssignStmt("x", new IntExp(5));
        Map<Variable, Type> map = new HashMap<Variable, Type>();
        map.put(new Variable("x"), new IntType());
        Map<Variable, Type> env = Typechecker.typecheckStmt(stmt, EMPTY_TYPE_ENV);
        env = Typechecker.typecheckStmt(assign, env);
        
        assertEquals(map, env);
    }
}
