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
package net.oneandone.mork.mapping;

import junit.framework.TestCase;
import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.semantics.Ag;
import net.oneandone.mork.semantics.AgBuffer;
import net.oneandone.mork.semantics.Attribute;
import net.oneandone.mork.semantics.Oag;
import net.oneandone.mork.semantics.Occurrence;
import net.oneandone.mork.semantics.Pusher;
import net.oneandone.sushi.util.IntBitSet;

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
