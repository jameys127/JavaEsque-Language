package com.jamey.compiler.Parser;

public class ParseResult<A> {
    public final A result;
    public final int position;
    
    public ParseResult(final A result, final int position){
        this.result = result;
        this.position = position;
    }
    public ParseResult(final A result){
        this.result = result;
        this.position = 0;
    }

    @Override
    public boolean equals(final Object other){
        if(other instanceof ParseResult){
            ParseResult<A> p = (ParseResult<A>)other;
            return (result.equals(p.result) &&
                    position == p.position);
        }else{
            return false;
        }
    }
}
