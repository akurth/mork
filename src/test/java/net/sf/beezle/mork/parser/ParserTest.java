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
import net.sf.beezle.mork.pda.PDA;

/**
 * Test that pda tables are generated without exceptions.
 * TODO: Test the generated parser on some input
 */
public class ParserTest extends TestCase {
    public void testMult() throws GenericException {
        check("S A A",
              "A B B",
              "B I");
    }

    /** LALR(0) grammar */
    public void testExpr() throws GenericException {
        check("S G #",
            "G E = E",
            "G f",
            "E T",
            "E E + T",
            "T f",
            "T T * f");
    }

    /** SLR(0) example */
    public void testExpr2() throws GenericException {
        check("S E #",
            "E E - T",
            "E T",
            "T F ^ T",
            "T F",
            "F ( E )",
            "F i");
    }

    /** Tremblay Sorenson, exercise 7-4.6.1 */
    public void testExercise1() throws GenericException {
        check("S a A d",
              "S a e c",
              "S b A c",
              "A e");
    }

    public void testExercise2() throws GenericException {
        check(2, "S A",
              "B",
              "C",
              "A B C A",
              "A a");
    }

    public void testBlocks() throws GenericException {
        check(1, "S E $",
              "E E E",
              "E ( )");
    }


    public static void check(String ... src) throws GenericException {
        check(0, src);
    }

    /**
     * Start symbol must be "S", symbol with example attribute
     * must be "I"
     */
    public static void check(int conflicts, String ... src) throws GenericException {
        Grammar grammar;
        PDA pda;
        ConflictHandler ch;
        ParserTable table;

        grammar = Grammar.forProductions(src);
        pda = PDA.create(grammar, grammar.firsts(1), 1);
        ch = new ConflictHandler(pda);
        table = pda.createTable(grammar.getSymbolCount(), ch);
        assertEquals(0, ch.resolvers());
        assertEquals(conflicts, ch.conflicts());
        assertTrue(table.getValueCount() > 0);
    }
}
