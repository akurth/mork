/**
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.grammar.Rule;
import net.sf.beezle.mork.mapping.ExceptionErrorHandler;
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
    public void simple() throws Exception {
        checkOk(2, 1, new String[] { "a" }, "S a");
    }

    @Test
    public void g1() throws Exception {
        checkOk(13, 1, new String[] { "baa" },
                "Z S",
                "S S b",
                "S b A a",
                "A a S c",
                "A a",
                "A a S b");
    }

    @Test
    public void lr2() throws Exception {
        checkOk(8, 2, new String[]{/*"baa", */"ba"},
                "Z S",
                "S Y a a",
                "S X a",
                "X b",
                "Y b"
        );
    }

    @Test
    public void lr3() throws Exception {
        checkOk(15, 3, new String[] { "xaaa", "xaab", "xaac" },
                "Z S",
                "S A a a a",
                "S B a a b",
                "S C a a c",
                "A x",
                "B x",
                "C x"
        );
    }

    private void checkOk(int states, int k, String[] ok, String ... prods) throws GenericException, IOException {
        Grammar grammar;

        grammar = Grammar.forProductions(prods);
        checkParsing(grammar, states, k, ok);
    }

    private void checkParsing(Grammar grammar, int states, int k, String[] ok) throws GenericException, IOException {
        IntBitSet terminals;
        PDA pda;
        ConflictHandler conflictHandler;
        final ParserTable table;
        Rule[] scannerRules;
        String str;
        FABuilder builder;
        ScannerFactory scannerFactory;
        final Parser parser;

        terminals = new IntBitSet();
        grammar.getTerminals(terminals);
        pda = check(states, k, grammar);
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
        System.out.println(table.toString(grammar));
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
                    int length;

                    length = table.getLength(production);
                    for (int i = 0; i < length; i++) {
                        parser.pop();
                    }
                    return null;
                }
            }, System.out);
        }
    }

    private PDA check(int states, int k, Grammar grammar) {
        PDA pda;

        pda = PDA.create(grammar, grammar.firsts(k), k, 1);
        // pda.print(System.out);
        assertEquals(states, pda.size() - 1 /* TODO: artificial end state */);
        return pda;
    }
}
