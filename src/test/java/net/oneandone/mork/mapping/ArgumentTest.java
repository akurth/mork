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

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.semantics.AgBuffer;
import net.oneandone.mork.semantics.Attribute;

import java.util.ArrayList;
import java.util.List;

public class ArgumentTest extends CompareBase {
    private Grammar grammar;
    private List<Argument> args;
    private List<List<Argument>> expected;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        args = new ArrayList<Argument>();
        expected = new ArrayList<List<Argument>>();
    }

    public void testEmpty() {
        sort();
    }

    public void testOne() {
        grammar(new String[] {
                    "X A"
                });
        arg("a", "XA", 0);
        sort();
    }

    public void testAlt() {
        grammar(new String[] {
                    "X A",
                        "X B",
                });
        arg("a", "XA", 0);
        arg("b", "XB", 0);
        sort();
    }

    public void testEmptyAlt() {
        grammar(new String[] {
            "X A",
            "X B",
            "X",
        });
        arg("a", "XA", 0);
        arg("b", "XB", 0);
        sort();
    }

    public void testIndirectAlt() {
        grammar(new String[] {
            "X I",
            "X A",
            "I B",
        });
        arg("a", "XA", 0);
        arg("b", "XIB", 0);
        sort();
    }

    public void testLT2() {
        grammar(new String[] {
                    "X A B",
                });
        arg("a", "XA", 0);
        arg("b", "XB", 1);
        sort();
    }

    public void testLTIndirect() {
        grammar(new String[] {
                    "X I B",
                    "I A",
                });
        arg("a", "XIA", 0);
        arg("b", "XB", 1);
        sort();
    }

    public void testLT3() {
        grammar(new String[] {
                    "X A B C",
                });
        arg("a", "XA", 0);
        arg("b", "XB", 1);
        arg("c", "XC", 2);
        sort();
    }

    public void testGT2() {
        grammar(new String[] {
                    "X B A",
                });
        arg("a", "XA", 1);
        arg("b", "XB", 0);
        sort();
    }

    public void testGT3() {
        grammar(new String[] {
                    "X C B A",
                });
        arg("a", "XA", 2);
        arg("b", "XB", 1);
        arg("c", "XC", 0);
        sort();
    }

    // optional argument problem
    public void testGToptional() {
        grammar(new String[] {
                    "X I J",
                    "X J",
                    "I A",
                    "J B"
                });
        arg("a", "XIA", 0);
        arg("b", "XJB", 1);
        sort();
    }

    public void testAltLT() {
        grammar(new String[] {
                    "X I C",
                    "I A",
                    "I B"
                });
        arg("a", "XIA", 0);
        arg("b", "XIB", 0);
        arg("c", "XC", 1);
        sort();
    }

    public void testAltLTAlt() {
        grammar(new String[] {
                    "X I J",
                        "I A",
                        "I B",
                        "J C",
                        "J D",
                });
        arg("a", "XIA", 0);
        arg("b", "XIB", 0);
        arg("c", "XJC", 1);
        arg("D", "XJD", 1);
        sort();
    }

    public void testUsedTwice() {
        grammar(new String[] {
                "S X P X",
                "X A",
                "X M B N",
                });
        arg("a", "SXA", 0);
        arg("b", "SXB", 0);
        sort();
    }

    public void testRecursed() {
        grammar(new String[] {
                "S X P",
                "X X",
                "X A",
                "X B",
                });
        arg("a", "SXA", 0);
        arg("b", "SXB", 0);
        sort();
    }

    public void testThirdBeforeFirstAndSecond() {
        grammar("S T X",
                "X A",
                "X B",
                "T C"
                );
        arg("a", "SXA", 1);
        arg("b", "SXB", 1);
        arg("c", "STC", 0);
        sort();
    }

    private void grammar(String ... prods) {
        grammar = Grammar.forProductions(prods);
    }

    private void arg(String attrName, String attrs, int expectedPos) {
        Argument arg;
        AgBuffer sems;

        sems = new AgBuffer((Attribute) null);
        attr = CopyBufferCompareTest.addTransport(sems, attrName, attrs, grammar);
        sems.setStart(attr);
        arg = new Argument(Path.MERGEABLE, sems, new ArrayList<Definition>()); // TODO: no sources
        args.add(arg);
        while (expected.size() <= expectedPos) {
            expected.add(new ArrayList<Argument>());
        }
        expected.get(expectedPos).add(arg);
    }

    private void sort() {
        List<Argument> reversed;
        int i;

        doSortAndMerge(args, expected, grammar);
        reversed = new ArrayList<Argument>();
        for (i = args.size() - 1; i >= 0; i--) {
            reversed.add(args.get(i));
        }
        doSortAndMerge(reversed, expected, grammar);
    }

    /** @param arguments   list of Arguments */
    private static void doSortAndMerge(List<Argument> arguments, List<List<Argument>> expected, Grammar grm) {
        List<List<Argument>> sorted;
        List<Argument> merged;
        int i;
        int max;
        Argument arg;
        List<List<Argument>> expectedMerged;
        List<Argument> tmp;

        sorted = doSort(arguments, expected);
        max = sorted.size();
        merged = new ArrayList<Argument>();
        expectedMerged = new ArrayList<List<Argument>>();
        for (i = 0; i < max; i++) {
            arg = Argument.merge(grm.getStart(), null, sorted.get(i));
            merged.add(arg);
            assertIncludes(arg, sorted.get(i));
            tmp = new ArrayList<Argument>();
            tmp.add(arg);
            expectedMerged.add(tmp);
        }
        doSort(merged, expectedMerged);
    }


    private static List<List<Argument>> doSort(List<Argument> arguments, List<List<Argument>> expected) {
        List<List<Argument>> sorted;

        sorted = RelatedArgument.sort(arguments);
        assertSorted(sorted, expected);
        return sorted;
    }

    private static void assertSorted(List sorted, List expected) {
        List merge;
        int i;
        int max;

        max = sorted.size();
        for (i = 0; i < max; i++) {
            merge = (List) sorted.get(i);
            assertIdentical((List) expected.get(i), merge);
            assertNE(merge);
            if (i + 1 < max) {
                assertLT(merge, (List) sorted.get(i + 1));
            }
        }
    }

    private static void assertIdentical(List exp, List computed) {
        int i;
        int max;

        max = exp.size();
        assertEquals(max, computed.size());
        for (i = 0; i < max; i++) {
            assertContained((Argument) exp.get(i), computed);
        }
    }
    private static void assertContained(Argument arg, List args) {
        int i;
        int max;

        max = args.size();
        for (i = 0; i < max; i++) {
            if (arg == args.get(i)) {
                return;
            }
        }
        assertTrue(false);
    }

    private static void assertNE(List args) {
        int i;
        int max;
        int j;
        Argument a;
        Argument b;

        max = args.size();
        for (i = 0; i < max; i++) {
            a = (Argument) args.get(i);
            for (j = i + 1; j < max; j++) {
                b = (Argument) args.get(j);
                assertNE(a, b);
            }
        }
    }
    private static void assertLT(List as, List bs) {
        int i;
        int max;

        max = as.size();
        for (i = 0; i < max; i++) {
            assertLT((Argument) as.get(i), bs);
        }
    }
    private static void assertLT(Argument a, List bs) {
        int i;
        int max;

        max = bs.size();
        for (i = 0; i < max; i++) {
            assertLT(a, (Argument) bs.get(i));
        }
    }
    private static void assertNE(Argument a, Argument b) {
        assertEquals(NE, a.compare(b));
        assertEquals(NE, b.compare(a));
        assertEquals(NE, a.compare(a));
        assertEquals(NE, b.compare(b));
    }
    private static void assertLT(Argument a, Argument b) {
        assertEquals(LT, a.compare(b));
        assertEquals(GT, b.compare(a));
        assertEquals(NE, a.compare(a));
        assertEquals(NE, b.compare(b));
    }

    private static void assertIncludes(Argument left, List rights) {
        int i;
        int max;
        Argument right;

        max = rights.size();
        for (i = 0; i < max; i++) {
            right = (Argument) rights.get(i);
            assertIncludes(left, right);
        }
    }

    private static void assertIncludes(Argument left, Argument right) {
        // TODO
    }
}
