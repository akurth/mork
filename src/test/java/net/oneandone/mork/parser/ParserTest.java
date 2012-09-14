/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.parser;

import junit.framework.TestCase;
import net.oneandone.mork.compiler.ConflictHandler;
import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.pda.PDA;

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
        pda = PDA.create(grammar, grammar.firsts(1), 1, 1);
        ch = new ConflictHandler(grammar);
        table = pda.createTable(grammar.getSymbolCount(), ch);
        assertEquals(0, ch.resolvers());
        assertEquals(conflicts, ch.conflicts());
        assertTrue(table.getValueCount() > 0);
    }
}
