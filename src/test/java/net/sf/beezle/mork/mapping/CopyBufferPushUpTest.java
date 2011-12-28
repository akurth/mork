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
import net.sf.beezle.mork.semantics.Ag;
import net.sf.beezle.mork.semantics.AgBuffer;
import net.sf.beezle.mork.semantics.Attribute;
import net.sf.beezle.mork.semantics.Oag;
import net.sf.beezle.mork.semantics.Occurrence;
import net.sf.beezle.mork.semantics.Pusher;
import net.sf.beezle.sushi.util.IntBitSet;

public class CopyBufferPushUpTest extends TestCase {
    private Attribute seed;
    private AgBuffer sems;
    private Grammar grammar;

    public void testDirect() {
        create("A a");
        assertValid(1);
    }

    public void testDirectOfs() {
        create("A X Y a");
        assertValid(1);
    }

    public void testIndirect() {
        create(
            "B A",
            "A a");
        assertValid(1);
    }

    public void testMult() {
        create(new String[] {
            "B A A",
            "A a",
        });
        assertValid(2);
    }

    public void testMultMult() {
        create(
            "C B B",
            "B A A",
            "A a");
        assertValid(4);
    }

    public void testHiddenMult() {
        create(
            "C A B",
            "B A",
            "A a");
        assertValid(2);
    }

    public void testRightRecursion() {
        create(
            "L I L",
            "L",
            "I a");
        assertValid(1);
    }

    public void testLeftRecursion() {
        create(
            "L L I",
            "L",
            "I a");
        assertValid(1);
    }

    public void testPlusRecursion() {
        create(
            "L L I",
            "L I",
            "I a");
        assertValid(1);
    }

    public void testMultRecursionA() {
        create(
            "L L I",
            "L",
            "I A A",
            "A a");
        assertValid(1);
    }

    public void testMultRecursionB() {
        create(
            "L L I I",
            "L",
            "I a");
        assertValid(1);
    }

    //--

    private void assertValid(int size) {
        Occurrence occ;
        Oag ag;
        Ag trueSems;

        sems = Pusher.run(false, seed, new IntBitSet(), grammar);
        System.out.println("sems raw");
        System.out.println(sems.toString());

        /* TODO: doesn't work since attribute card is wrong:
        trueSems = new SemanticsBuffer();
        sems.createSemanticsBuffer(trueSems, new Transport(g));

        System.out.println("sems");
        ag = trueSems.createSemantics(g, new ArrayList());
         System.out.println(ag.toString(grm.getSymbolTable(), g)); */
    }

    /**
     * All symbols name have to be single characters.
     */
    private void create(String ... prods) {
        int sym;

        grammar = Grammar.forProductions(prods);
        sym = grammar.getSymbolTable().indexOf("a");
        if (sym == -1) {
            throw new IllegalArgumentException("missing seed symbol: a");
        }
        seed = new Attribute(sym, "a");
    }
}
