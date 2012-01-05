package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.grammar.Rule;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.parser.TreeBuilder;
import net.sf.beezle.mork.regexpr.Range;
import net.sf.beezle.mork.scanner.FABuilder;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.mork.scanner.Scanner;
import net.sf.beezle.mork.scanner.ScannerFactory;
import net.sf.beezle.mork.semantics.SemanticError;
import net.sf.beezle.sushi.util.IntBitSet;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class PDATest {
    @Test
    public void simple() {
        check(2, 1, "S a");
    }

    @Test
    public void g1() {
        check(13, 1,
                "Z S",
                "S S b",
                "S b A a",
                "A a S c",
                "A a",
                "A a S b");
    }

    @Test
    public void lr2() throws GenericException {
        Grammar grammar;

        grammar = Grammar.forProductions("Z S",
                "S Y a a",
                "S X a",
                "X b",
                "Y b"
        );
        checkParsing(grammar, 2, new String[]{"baa"});
    }

    private void checkParsing(Grammar grammar, int k, String[] ok) throws GenericException {
        IntBitSet terminals;
        PDA pda;
        ConflictHandler conflictHandler;
        ParserTable table;
        Rule[] scannerRules;
        String str;
        FABuilder builder;
        ScannerFactory scannerFactory;
        Parser parser;

        terminals = new IntBitSet();
        grammar.getTerminals(terminals);
        pda = check(8, k, grammar);
        pda.print(System.out);
        conflictHandler = new ConflictHandler(grammar);
        table = pda.createTable(pda.getEofSymbol(), conflictHandler);
        scannerRules = new Rule[terminals.size()];
        for (int i = 0, terminal = terminals.first(); terminal != -1; terminal = terminals.next(terminal)) {
            str = grammar.getSymbolTable().get(terminal);
            if (str.length() != 1) {
                throw new UnsupportedOperationException(str);
            }
            scannerRules[i++] = new Rule(terminal, new Range(str.charAt(0)));
        }
        builder = FABuilder.run(scannerRules, terminals, grammar.getSymbolTable(), null);
        scannerFactory = ScannerFactory.create(builder.getFA(), builder.getErrorState(), table, new IntBitSet(), null, null);
        parser = new Parser(table, conflictHandler.report(null, grammar), scannerFactory);
        parser.setErrorHandler(new ExceptionErrorHandler());
        for (String s : ok) {
            parser.run(new Position("test"), new StringReader(s), new TreeBuilder() {
                @Override
                public void open(Scanner scanner, Parser parser) {
                }

                @Override
                public Object createTerminal(int terminal) throws IOException {
                    return null;
                }

                @Override
                public Object createNonterminal(int production) throws SemanticError {
                    return null;
                }
            }, null);
        }
    }

    private PDA check(int states, int k, String ... prods) {
        return check(states, k, Grammar.forProductions(prods));
    }

    private PDA check(int states, int k, Grammar grammar) {
        PDA pda;

        pda = PDA.create(grammar, k);
        // pda.print(System.out);
        assertEquals(states, pda.size() - 1 /* TODO: artificial end state */);
        return pda;
    }
}
