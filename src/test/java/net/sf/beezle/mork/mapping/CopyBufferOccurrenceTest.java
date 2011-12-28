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

package net.sf.beezle.mork.mapping;

import junit.framework.TestCase;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.semantics.AgBuffer;
import net.sf.beezle.mork.semantics.Attribute;
import net.sf.beezle.mork.semantics.Occurrence;

public class CopyBufferOccurrenceTest extends TestCase {
    private Attribute start;
    private AgBuffer sems;

    public void testDirect() {
        create(new String[] {
            "A B",
        }, "AB");
        assertOccurrence(1, 1);
    }

    public void testIndirect() {
        create(new String[] {
            "A B",
            "B C",
        }, "ABC");
        assertOccurrence(1, 1);
    }

    public void testAlt() {
        create(new String[] {
            "A i B",
            "A j j B",
        }, "AB");
        assertOccurrence(1, 1);
    }

    public void testZeroOne() {
        create(new String[] {
            "A B",
            "A",
        }, "AB");
        assertOccurrence(0, 1);
    }

    public void testOneTwo() {
        create(new String[] {
            "A B",
            "A B B",
        }, "AB");
        assertOccurrence(1, 2);
    }

    public void testTwoFour() {
        create(new String[] {
            "A B B",
            "A B B B B",
        }, "AB");
        assertOccurrence(2, 4);
    }

    public void testZeroThree() {
        create(new String[] {
            "A",
            "A B B B",
        }, "AB");
        assertOccurrence(0, 3);
    }
    public void testZeroOneFour() {
        create(new String[] {
            "A B",
            "A",
            "A B B B B",
        }, "AB");
        assertOccurrence(0, 4);
    }

    public void testMultTwo() {
        create(new String[] {
            "A B B",
            "B C C",
        }, "ABC");
        assertOccurrence(4, 4);
    }

    public void testMultOpt() {
        create(new String[] {
            "A B B",
            "B C C",
            "C D",
            "C",
        }, "ABCD");
        assertOccurrence(0, 4);
    }

    public void testLeftRecursion() {
        create(new String[] {
            "A A B",
            "A"
        }, "AB");
        assertOccurrence(0, Occurrence.UNBOUNDED);
    }
    public void testRightRecursion() {
        create(new String[] {
            "A B A",
            "A"
        }, "AB");
        assertOccurrence(0, Occurrence.UNBOUNDED);
    }

    public void testPlus() {
        create(new String[] {
            "A A B",
            "A B"
        }, "AB");
        assertOccurrence(1, Occurrence.UNBOUNDED);
    }

    public void testPlusTwo() {
        create(new String[] {
            "A A B",
            "A B B"
        }, "AB");
        assertOccurrence(1, Occurrence.UNBOUNDED);  // TODO: min 2 ...
    }

    public void testPlusTwo2() {
        create(new String[] {
            "A A B B",
            "A B B"
        }, "AB");
        assertOccurrence(2, Occurrence.UNBOUNDED);
    }

    public void testInderectRecursion() {
        create(new String[] {
            "A B C",
            "A",
            "B A"
        }, "ABC");
        assertOccurrence(0, Occurrence.UNBOUNDED);
    }

    public void testBracketRecursion() {
        create(new String[] {
            "A a A b",
            "A B",
        }, "AB");
        assertOccurrence(1, 1);
    }

    //--

    private void assertOccurrence(int min, int max) {
        Occurrence occ;

        occ = sems.calcOccurrence(start);
        assertEquals("min", min, occ.min);
        assertEquals("max", max, occ.max);
    }

    /**
     * All symbols name have to be single characters.
     */
    private void create(String[] prods, String as) {
        Grammar grammar;

        grammar = Grammar.forProductions(prods);
        sems = new AgBuffer((Attribute) null);
        start = CopyBufferCompareTest.addTransport(sems, "a", as, grammar);
    }
}
