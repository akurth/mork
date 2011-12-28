/*
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

package net.sf.beezle.mork.parser;

import junit.framework.TestCase;
import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.misc.StringArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Test that pda tables are generated without exceptions.
 * TODO: Test the generated parser on some input
 */
public class ParserTest extends TestCase {
    public void testMult() throws GenericException {
        check(new String[][] {
            { "S",  "A", "A" },
            { "A",  "B", "B" },
            { "B",  "I" }
        });
    }

    /** LALR(0) grammar */
    public void testExpr() throws GenericException {
        check(new String[][] {
            { "S", "G", "#" },
            { "G", "E", "=", "E" },
            { "G", "f" },
            { "E", "T" },
            { "E", "E", "+", "T" },
            { "T", "f" },
            { "T", "T", "*", "f" }
        });
    }

    /** SLR(0) example */
    public void testExpr2() throws GenericException {
        check(new String[][] {
            { "S", "E", "#" },
            { "E", "E", "-", "T" },
            { "E", "T" },
            { "T", "F", "^", "T" },
            { "T", "F" },
            { "F", "(", "E", ")" },
            { "F", "i" }
        });
    }

    /** Tremblay Sorenson, exercise 7-4.6.1 */
    public void testExercise1() throws GenericException {
        check(new String[][] {
            { "S", "a", "A", "d" },
            { "S", "a", "e", "c" },
            { "S", "b", "A", "c" },
            { "A", "e" }
        });
    }

    public void testExercise2() throws GenericException {
        check(new String[][] {
            { "S", "A" },
            { "B" },
            { "C" },
            { "A", "B", "C", "A" },
            { "A", "a" }
        });
    }

    public void testBlocks() throws GenericException {
        check(new String[][] {
            { "S", "E", "$" },
            { "E", "E", "E" },
            { "E", "(", ")" }
        });
    }

    public static StringArrayList symbolTable;

    /**
     * Start symbol must be "S", symbol with example attribute
     * must be "I"
     */
    public static void check(String[][] src) throws GenericException {
        Grammar grammar;
        int startSymbol;
        PDA pda;
        ParserTable table;

        grammar = Grammar.forSymbols(src);
        symbolTable = grammar.getSymbolTable();
        startSymbol = symbolTable.indexOf("S");

        pda = PDA.create(grammar, startSymbol);

        table = pda.createTable(grammar.getSymbolCount(), new ConflictHandler(grammar));

        // TODO:
        //   assertTrue("no conflicts", conflicts.isEmpty() );
        // there are conflicts in testExercise2 and testBlocks

        // TODO: more tests
    }
}
