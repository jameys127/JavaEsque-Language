package com.jamey.compiler.Typechecker;

import java.util.Map;

import com.jamey.compiler.Parser.*;

public class Typechecker {
    public static Type typecheckBin(final BinaryExp exp,
                                    final Map<Variable, Type> typeEnv)
                                    throws TypecheckerErrorException{
        final Type left = typecheckExp(exp.l(), typeEnv);
        final Type right = typecheckExp(exp.r(), typeEnv);
        if((exp.op() instanceof PlusOp ||
            exp.op() instanceof MinusOp ||
            exp.op() instanceof MultOp ||
            exp.op() instanceof DivOp) &&
            left instanceof IntType &&
            right instanceof IntType){
            return new IntType();
        }else{
            throw new TypecheckerErrorException("No such binary operator recognized: " + exp.op().toString());
        }
    }
    /*
     * BinaryExp - done
     * BooleanExp - done
     * IntExp - done
     * MethodCallExp
     * NewExp
     * ParenExp
     * PrintlnExp
     * StrExp
     * ThisExp
     */
    public static Type typecheckExp(final Exp exp, 
                                    final Map<Variable, Type> typeEnv)
                                     throws TypecheckerErrorException{
        if(exp instanceof IntExp){
            return new IntType();
        }else if(exp instanceof BooleanExp){
            return new BoolType();
        } else if (exp instanceof VarExp){
            final Variable variable = new Variable(((VarExp)exp).name());
            if(typeEnv.containsKey(variable)){
                return typeEnv.get(variable);
            }else {
                throw new TypecheckerErrorException("Variable not in scope: " + variable.toString());
            }
        }else if(exp instanceof BinaryExp){
            return typecheckBin((BinaryExp)exp, typeEnv);
        }else{
            assert(false);
            throw new TypecheckerErrorException("Unrecognized expression: " + exp.toString());
        }
    }

    public static void typecheckVardec(final VardecStmt stmt,
                                        final Map<Variable, Type> typeEnv)
                                        throws TypecheckerErrorException{
        
    }

    public static Type typecheckStmt(final Stmt stmt,
                                    final Map<Variable, Type> typeEnv)
                                    throws TypecheckerErrorException{
        if(stmt instanceof VardecStmt){

        }
    }
}
