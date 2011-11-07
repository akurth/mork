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

package de.mlhartme.mork.mapping;

import junit.framework.TestCase;

import net.sf.beezle.sushi.util.IntBitSet;

import de.mlhartme.mork.grammar.Grammar;
import de.mlhartme.mork.semantics.Ag;
import de.mlhartme.mork.semantics.Attribute;
import de.mlhartme.mork.semantics.AgBuffer;
import de.mlhartme.mork.semantics.Oag;
import de.mlhartme.mork.semantics.Occurrence;
import de.mlhartme.mork.semantics.Pusher;

public class CopyBufferPushUpTest extends TestCase {
    private Attribute seed;
    private AgBuffer sems;
    private Grammar grm;

    public void testDirect() {
        create(new String[] {
            "A a",
        });
        assertValid(1);
    }

    public void testDirectOfs() {
        create(new String[] {
            "A X Y a",
        });
        assertValid(1);
    }

    public void testIndirect() {
        create(new String[] {
            "B A",
            "A a",
        });
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
        create(new String[] {
            "C B B",
            "B A A",
            "A a",
        });
        assertValid(4);
    }

    public void testHiddenMult() {
        create(new String[] {
            "C A B",
            "B A",
            "A a",
        });
        assertValid(2);
    }

    public void testRightRecursion() {
        create(new String[] {
            "L I L",
            "L",
            "I a",
        });
        assertValid(1);
    }

    public void testLeftRecursion() {
        create(new String[] {
            "L L I",
            "L",
            "I a",
        });
        assertValid(1);
    }

    public void testPlusRecursion() {
        create(new String[] {
            "L L I",
            "L I",
            "I a",
        });
        assertValid(1);
    }

    public void testMultRecursionA() {
        create(new String[] {
            "L L I",
            "L",
            "I A A",
            "A a",
        });
        assertValid(1);
    }

    public void testMultRecursionB() {
        create(new String[] {
            "L L I I",
            "L",
            "I a",
        });
        assertValid(1);
    }
    //---------------------------------

    private void assertValid(int size) {
        Occurrence occ;
        Oag ag;
        Ag trueSems;

        sems = Pusher.run(false, seed, new IntBitSet(), grm);
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
    private void create(String[] prods) {
        int sym;

        grm = Grammar.forProductions(prods);
        sym = grm.getSymbolTable().indexOf("a");
        if (sym == -1) {
            throw new IllegalArgumentException("missing seed symbol: a");
        }
        seed = new Attribute(sym, "a");
    }
}
