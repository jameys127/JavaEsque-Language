# Java-Esque
## Website
[Try it out on my website - javaesquecompiler.dev](javaesquecompiler.dev)
## Target Language:
Javascript

## Language Description:
(Pathetic) object-oriented programming.  The goal is for me to better understand how object-oriented programming languages work.  I want to implement a Java-like language with classes and subclasses. This is my solo attempt at a school team project that I did for my last semester in class. I want to attempt to add things that are missing in the original like string literals, return path validation, and maybe some other added bits and bobs. It compiles to javascript.

## Key Features:
Objects + methods with class-based inheritance, subtyping, checking if a variable is initialized before use, checking if void is used as a value, checking that a function returning non-void always returns, non-S-expression-based syntax.

## Concrete Syntax:
- var is a variable
- classname is the name of a class
- methodname is the name of a method
- str is a string
- i is an integer

```
type ::= `Int` | `Boolean` | `Void` | Built-in types
         classname class type; includes Object and String

comma_exp ::= [exp (`,` exp)*]

primary_exp ::=
  var | str | i | Variables, strings, and integers are     
                  expressions

  '"' str '"' | String Literal

  `(` exp `)` | Parenthesized expressions

  `this` | Refers to my instance

  `true` | `false` | Booleans

  `println` `(` exp `)` | Prints something to the terminal

  `new` classname `(` comma_exp `)` Creates a new object

call_exp ::= primary_exp (`.` methodname `(` comma_exp `)`)*

mult_exp ::= call_exp ((`*` | `/`) call_exp)*

add_exp ::= mult_exp ((`+` | `-`) mult_exp)*

bool_exp ::= add_exp (('<' | '<=' |
                       '>' | '>=' |
                       '==') add_exp)*

exp ::= bool_exp

vardec ::= type var

stmt ::= exp `;` | Expression statements
         vardec `;` | Variable declaration
         var `=` exp `;` | Assignment
         `while` `(` exp `)` stmt | while loops
         `break` `;` | break
         `return` [exp] `;` | return, possibly void
         if with optional else
         `if` `(` exp `)` stmt [`else` stmt] | 
         `{` stmt* `}` Block

comma_vardec ::= [vardec (`,` vardec)*]

methoddef ::= `method` methodname `(` comma_vardec `)` type
              `{` stmt* `}`

constructor ::= `init` `(` comma_vardec `)` `{`
                [`super` `(` comma_exp `)` `;` ]
                stmt*
                `}`
classdef ::= `class` classname [`extends` classname] `{`
             (vardec `;`)*
             constructor
             methoddef*
             `}`

program ::= classdef* stmt+  stmt+ is the entry point
```

```
Example (animals with a speak method):

class Animal {
  init() {}
  method speak() void { return println(0); }
}
class Cat extends Animal {
  init() { super(); }
  method speak() void { return println(1); }
}
class Dog extends Animal {
  init() { super(); }
  method speak() void { return println(2); }
}

Animal cat;
Animal dog;
cat = new Cat();
dog = new Dog();
cat.speak();
dog.speak();
```

## Running the Code ##
You can test out my compiler at this website I created using react for the front end. It has a text editor where you can write your program, as well as a documentation section if you want to learn more about the syntax of this language.
[Test it out here!](javaesquecompiler.dev)
