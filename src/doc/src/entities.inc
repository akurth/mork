<!ENTITY grammarText "[PARSER]

#
# overall structure

Grammar             ::= Parser Scanner ;
Parser              ::= &#34;[&#34; &#34;PARSER&#34; &#34;]&#34; Rule+ ;
Scanner             ::= &#34;[&#34; &#34;SCANNER&#34; &#34;]&#34; Priorities WhiteOpt Rule* ;
Priorities          ::= UsePriorities | NoPriorities;
UsePriorities       ::= ;
NoPriorities        ::= &#34;nopriorities;&#34;;
WhiteOpt            ::= (&#34;white&#34; &#34;=&#34; SymbolSet &#34;;&#34;)? ;
SymbolSet           ::= ( Symbol (&#34;,&#34; Symbol)* )? ;


#
# symbols

Symbol              ::= StringSymbol | IdentifierSymbol ;
StringSymbol        ::= StringLiteral ;
IdentifierSymbol    ::= Identifier ;
Rule                ::= Subject &#34;::=&#34; RegExpr &#34;;&#34; ;
Subject             ::= Symbol ;

#
# regular expressions

RegExpr             ::= Choice ;
Choice              ::= Choice &#34;|&#34; Sequence | Sequence ;
Sequence            ::= (Factor | Restriction) * ;
Restriction         ::= Factor &#34;-&#34; Factor;
Factor              ::= Operation | Reference | Range | &#34;(&#34; RegExpr &#34;)&#34; ;
Operation           ::= Star | Plus | Option | Times | Not  ;

Star                ::= Factor &#34;*&#34; ;
Plus                ::= Factor &#34;+&#34; ;
Option              ::= Factor &#34;?&#34; ;
Times               ::= Factor &#34;:&#34; IntegerLiteral ;
Not                 ::= Factor &#34;!&#34; ;

Reference           ::= Symbol ;

Range               ::= Atom (&#34;..&#34; Atom)? ;
Atom                ::= CharacterLiteral | Code ;
Code                ::= IntegerLiteral ;

[SCANNER]
    white = WhiteSpace, EndOfLineComment, TraditionalComment ;


IntegerLiteral     ::= &#39;0&#39;
                     | &#39;1&#39;..&#39;9&#39; &#39;0&#39;..&#39;9&#39;*
                     | &#39;0&#39; (&#39;x&#39;|&#39;X&#39;)
                           (&#39;0&#39;..&#39;9&#39; | &#39;a&#39;..&#39;f&#39; | &#39;A&#39;..&#39;F&#39;)+
                     | &#39;0&#39; (&#39;0&#39;..&#39;7&#39;)+ ;

StringLiteral      ::= &#39;&#34;&#39; ((&#39;\\&#39; 0..65535)
                        | (&#39;\\&#39; | &#39;&#34;&#39; | &#39;\n&#39; | &#39;\r&#39;)!)+ &#39;&#34;&#39; ;

CharacterLiteral   ::= &#39;\&#39;&#39; ((&#39;\\&#39; 0..65535) |
                           (&#39;\\&#39; |&#39;\&#39;&#39; |&#39;\n&#39; | &#39;\r&#39;)!)+ &#39;\&#39;&#39; ;

Identifier ::=
   ( 0x0024..0x0024 | 0x0041..0x005a | 0x005f..0x005f |
     0x0061..0x007a | 0x00a2..0x00a5 | 0x00aa..0x00aa |
     0x00b5..0x00b5 | 0x00ba..0x00ba | 0x00c0..0x00d6 |
     0x00d8..0x00f6 | 0x00f8..0x00ff )
   ( 0x0000..0x0008 | 0x000e..0x001b | 0x0024..0x0024 |
     0x0030..0x0039 | 0x0041..0x005a | 0x005f..0x005f |
     0x0061..0x007a | 0x007f..0x009f | 0x00a2..0x00a5 |
     0x00aa..0x00aa | 0x00b5..0x00b5 | 0x00ba..0x00ba |
     0x00c0..0x00d6 | 0x00d8..0x00f6 | 0x00f8..0x00ff )* ;

#
# white space

WhiteSpace         ::= ( &#39; &#39; | &#39;\t&#39; | &#39;\n&#39; | &#39;\f&#39; | &#39;\r&#39; )+ ;
EndOfLineComment   ::= &#39;#&#39; (&#39;\n&#39; | &#39;\r&#39;)!* (&#39;\n&#39; | &#39;\r&#39;) ;
TraditionalComment ::=
  &#34;/*&#34;
         ( (&#39;*&#39;! | &#39;*&#39;+ (&#39;*&#39; | &#39;/&#39;)!)*
         | &#39;*&#39;!* &#39;*&#39; (&#39;*&#39; | (&#39;*&#39; | &#39;/&#39;)! &#39;*&#39;!* &#39;*&#39;)*
         )
  &#34;*/&#34; ;
">
<!ENTITY mapperText "[PARSER]

#
# overall structure

Mapper              ::= MapperName SyntaxFile Imports Definitions ;
MapperName          ::= &#34;mapper&#34; Name &#34;;&#34; ;
SyntaxFile          ::= Grammar | Dtd ;
Grammar             ::= &#34;grm&#34; &#34;=&#34; StringLiteral &#34;;&#34; ;
Dtd                 ::= &#34;dtd&#34; &#34;=&#34; StringLiteral &#34;;&#34; ;
Imports             ::= Import* ;
Import              ::= &#34;import&#34; PackageName &#34;:&#34; Class (&#34;,&#34; Class)* &#34;;&#34;;
Class               ::= Identifier (&#34;->&#34; Identifier)? ;
PackageName         ::= Name ;
Name                ::= Identifier (&#34;.&#34; Identifier)* ;

#
# Constructors

Constructor         ::= ClassRef | MemberRef | Internal | Copy;
ClassRef            ::= Identifier ;
MemberRef           ::= Identifier &#34;.&#34; Identifier ;
Internal            ::= &#34;[&#34; Identifier &#34;]&#34; ;
Copy                ::= &#34;(&#34; Identifier &#34;)&#34; ;

#
# Attributes

Definitions         ::= Group* ;
Group               ::= Symbol Attribute+ ;
Symbol              ::= StringSymbol | IdentifierSymbol ;
StringSymbol        ::= StringLiteral ;
IdentifierSymbol    ::= Identifier ;
Attribute           ::= AttributeName &#34;=>&#34; Constructor Visibility ;
AttributeName       ::= (&#34;:&#34; Identifier)? ;

#
# visibility

Visibility          ::= Implicit | Explicit ;
Implicit            ::= &#34;;&#34; ;
Explicit            ::= &#34;:&#34; (&#34;>&#34; Path)* &#34;;&#34; ;
Path                ::= ImplicitPath | LocalPath | NormalPath ;
ImplicitPath        ::= &#34;\\\\*&#34; ;
LocalPath           ::= Identifier ;
NormalPath          ::= Step+ ;
Step                ::= Move Identifier ;
Move                ::= Ups | Up | Downs | Down ;
Ups                 ::= &#34;\\\\&#34; ;
Up                  ::= &#34;\\&#34; ;
Downs               ::= &#34;//&#34; ;
Down                ::= &#34;/&#34; ;


[SCANNER]
    white = WhiteSpace, EndOfLineComment, TraditionalComment ;


StringLiteral      ::= &#39;&#34;&#39; ((&#39;\\&#39; 0..65535)
                        | (&#39;\\&#39; | &#39;&#34;&#39; | &#39;\n&#39; | &#39;\r&#39;)!)+ &#39;&#34;&#39; ;

Identifier ::=
   ( 0x0024..0x0024 | 0x0041..0x005a | 0x005f..0x005f |
     0x0061..0x007a | 0x00a2..0x00a5 | 0x00aa..0x00aa |
     0x00b5..0x00b5 | 0x00ba..0x00ba | 0x00c0..0x00d6 |
     0x00d8..0x00f6 | 0x00f8..0x00ff )
   ( 0x0000..0x0008 | 0x000e..0x001b | 0x0024..0x0024 |
     0x0030..0x0039 | 0x0041..0x005a | 0x005f..0x005f |
     0x0061..0x007a | 0x007f..0x009f | 0x00a2..0x00a5 |
     0x00aa..0x00aa | 0x00b5..0x00b5 | 0x00ba..0x00ba |
     0x00c0..0x00d6 | 0x00d8..0x00f6 | 0x00f8..0x00ff )* ;

#
# white space

WhiteSpace         ::= ( &#39; &#39; | &#39;\t&#39; | &#39;\n&#39; | &#39;\f&#39; | &#39;\r&#39; )+ ;
EndOfLineComment   ::= &#39;#&#39; (&#39;\n&#39; | &#39;\r&#39;)!* (&#39;\n&#39; | &#39;\r&#39;) ;
TraditionalComment ::=
  &#39;/&#39; &#39;*&#39;
         ( (&#39;*&#39;! | &#39;*&#39;+ (&#39;*&#39; | &#39;/&#39;)!)*
         | &#39;*&#39;!* &#39;*&#39; (&#39;*&#39; | (&#39;*&#39; | &#39;/&#39;)! &#39;*&#39;!* &#39;*&#39;)*
         )
  &#39;*&#39; &#39;/&#39; ;
">
<!ENTITY jarText "command &#34;create jar&#34;: &#34;Creates a jar file&#34;
    jar: &#34;jar file name&#34;;
    dir: &#34;directory&#34;;
{
    run &#34;jar cvf &#34; + jar + &#34; &#34; + dir;
}
">
<!ENTITY MainText "package command;

import de.ui.sushi.mapping.Mapper;

public class Main {
    public static void main(String[] args) {
        Mapper mapper;
        Object[] tmp;
        Command command;

        if (args.length != 1) {
            System.out.println(&#34;command: add frontends to command line tools&#34;);
            System.out.println(&#34;  usage: command.Main &lt;command file>&#34;);
        } else {
            mapper = new Mapper(&#34;command.Mapper&#34;);
            tmp = mapper.run(args[0]);
            if (tmp == null) {
                // runOrMessage has issued an error message
                System.exit(1);
            }
            command = (Command) tmp[0];
            command.run();
        }
        System.exit(0);     // just returning doesn&#39;t kill the gui threads
    }
}
">
<!ENTITY commandText "[PARSER]

Command      ::= &#34;command&#34; String &#34;:&#34; String Declarations &#34;{&#34; Line &#34;}&#34; ;
Declarations ::= Variable* ;
Variable     ::= Identifier &#34;:&#34; String &#34;;&#34; ;
Line         ::= &#34;run&#34; Expression (&#34;+&#34; Expression)* &#34;;&#34; ;
Expression   ::= Constant | Reference;
Constant     ::= String ;
String       ::= Literal ;
Reference    ::= Identifier ;

[SCANNER]
    white = Space, Comment ;

Letter       ::= &#39;A&#39;..&#39;Z&#39; | &#39;a&#39;..&#39;z&#39;;
Digit        ::= &#39;0&#39;..&#39;9&#39;;
Space        ::= (&#39;\u0020&#39; | &#39;\b&#39; | &#39;\t&#39; | &#39;\n&#39; | &#39;\f&#39; | &#39;\r&#39; )+;
Comment      ::= &#39;#&#39; &#39;\u0020&#39;..&#39;\u007f&#39;* (&#39;\n&#39;|&#39;\r&#39;);
Literal      ::= &#39;&#34;&#39; ( (&#39;\\&#39; 0..65535) | (&#39;\\&#39; | &#39;&#34;&#39; | &#39;\n&#39; | &#39;\r&#39;)!)+ &#39;&#34;&#39;;
Identifier   ::= Letter (Letter | Digit)* ;
">
<!ENTITY MapperText "mapper command.Mapper;

grm = &#34;command.grm&#34;;

import command:
    Command, Declarations, Line, Constant, Reference, Variable;
import de.ui.sushi.semantics:
    BuiltIn;

Command         => Command;
Declarations    => Declarations:
                     > \\Command
                     > \\Command//Reference;
Variable        => Variable;
Line            => Line;
Reference       => Reference;
Constant        => Constant;

Identifier      => [text];

String          => BuiltIn.parseString;
Literal         => [text];
">
<!ENTITY DeclarationsText "package command;

public class Declarations {
    private Variable[] vars;

    public Declarations(Variable[] vars) throws Failure {
        this.vars = vars;
        checkDuplicates();
    }

    /**
     * Throws Failure if there are multiple variables with the same name.
     */
    private void checkDuplicates() throws Failure {
        int i;
        Variable v;
        String name;

        for (i = 0; i &lt; vars.length; i++) {
            v = vars[i];
            name = v.getName();
            if (lookup(name) != v) {
                throw new Failure(&#34;duplicate variable name: &#34; + name);
            }
        }
    }

    public Variable lookup(String name) {
        int i;

        for (i = 0; i &lt; vars.length; i++) {
            if (vars[i].getName().equals(name)) {
                return vars[i];
            }
        }
        return null;
    }

    public boolean runFrontend(String title, String description) {
        Frontend frontend;
        boolean result;
        int i;

        frontend = new Frontend(title, description, vars.length);
        for (i = 0; i &lt; vars.length; i++) {
            frontend.setLabel(i, vars[i].getLabel());
        }
        result = frontend.run();
        for (i = 0; i &lt; vars.length; i++) {
            vars[i].set(frontend.getValue(i));
        }

        return result;
    }
}
">
<!ENTITY LineText "package command;

/**
 * A command line is a sequence of expression.
 */

public class Line {
    /**
     * Concatenating these expressions forms the command line
     * to be executed.
     */
    private Expression[] expressions;

    public Line(Expression[] expressions) {
        this.expressions = expressions;
    }

    public String eval() {
        StringBuffer buffer;
        int i;

        buffer = new StringBuffer();
        for (i = 0; i &lt; expressions.length; i++) {
            buffer.append(expressions[i].eval());
        }
        return buffer.toString();
    }
}
">
<!ENTITY ReferenceText "package command;

/** Variable reference. */

public class Reference extends Expression {
    /** variable referenced by this expression. */
    private Variable var;

    public Reference(Declarations decls, String identifier) throws Failure {
        var = decls.lookup(identifier);
        if (var == null) {
            throw new Failure(&#34;unkown identifier: &#34; + identifier);
        }
    }

    public String eval() {
        return var.get();
    }
}
">
<!ENTITY frontFile SYSTEM "file:/home/mhm/java/mork/doc/src/front.xml">
<!ENTITY introductionFile SYSTEM "file:/home/mhm/java/mork/doc/src/introduction.xml">
<!ENTITY setupFile SYSTEM "file:/home/mhm/java/mork/doc/src/setup.xml">
<!ENTITY firststepsFile SYSTEM "file:/home/mhm/java/mork/doc/src/firststeps.xml">
<!ENTITY syntaxFile SYSTEM "file:/home/mhm/java/mork/doc/src/syntax.xml">
<!ENTITY mapperFile SYSTEM "file:/home/mhm/java/mork/doc/src/mapper.xml">
<!ENTITY compilationFile SYSTEM "file:/home/mhm/java/mork/doc/src/compilation.xml">
<!ENTITY usageFile SYSTEM "file:/home/mhm/java/mork/doc/src/usage.xml">
<!ENTITY sourcesFile SYSTEM "file:/home/mhm/java/mork/doc/src/sources.xml">
<!ENTITY backFile SYSTEM "file:/home/mhm/java/mork/doc/src/back.xml">
