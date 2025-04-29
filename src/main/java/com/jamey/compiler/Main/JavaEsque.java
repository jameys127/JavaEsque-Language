package com.jamey.compiler.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import com.jamey.compiler.Lexer.TokenizerException;
import com.jamey.compiler.CodeGenerator.CodeGen;
import com.jamey.compiler.CodeGenerator.CodeGenException;
import com.jamey.compiler.Lexer.Token;
import com.jamey.compiler.Lexer.Tokenizer;
import com.jamey.compiler.Parser.ParserException;
import com.jamey.compiler.Parser.Parser;
import com.jamey.compiler.Parser.Program;
import com.jamey.compiler.Typechecker.TypecheckerErrorException;
import com.jamey.compiler.Typechecker.Typechecker;

public class JavaEsque {
    public static void usage(){
        System.out.println("Takes:");
        System.out.println("-Input JavaEsque file");
        System.out.println("-Output JavaScript file");
    }
    public static String readFileToString(final String fileName) throws IOException{
        return Files.readString(new File(fileName).toPath());
        
    }
    public static void main(String[] args) throws TokenizerException,
                                        ParserException, 
                                        TypecheckerErrorException, 
                                        CodeGenException, 
                                        IOException{
        if(args.length != 2){
            usage();
        }else{
            final String input = readFileToString(args[0]);
            final ArrayList<Token> tokens = new Tokenizer(input).tokenize();
            final Program program = new Parser(tokens).parseWholeProgram();
            Typechecker.typecheckProgram(program);
            CodeGen.writeProgram(program, new File(args[1]));
        }
    }
}
