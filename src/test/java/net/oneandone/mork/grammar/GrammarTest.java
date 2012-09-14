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
package net.oneandone.mork.grammar;

import net.oneandone.mork.misc.StringArrayList;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GrammarTest {
    @Test
    public void normal() {
        Grammar g;

        g = Grammar.forProductions("S A B", "A a b", "B x y");
        assertEquals(3, g.getProductionCount());
        assertEquals(1, g.getAlternativeCount(g.getSymbolTable().indexOf("S")));
    }

    @Test
    public void prefix() {
        Grammar g;
        Map<Integer, PrefixSet> firsts;
        StringArrayList symbolTable;
        int k = 1;

        g = Grammar.forProductions("Z S",
                "S S b",
                "S b A a",
                "A a S c",
                "A a",
                "A a S b");
        symbolTable = g.getSymbolTable();
        firsts = g.firsts(k);
        assertEquals(6, firsts.size());
        assertEquals(PrefixSet.one(symbolTable.indexOf("b")), firsts.get(symbolTable.indexOf("Z")));
        assertEquals(PrefixSet.one(symbolTable.indexOf("b")), firsts.get(symbolTable.indexOf("S")));
        assertEquals(PrefixSet.one(symbolTable.indexOf("a")), firsts.get(symbolTable.indexOf("A")));
    }

    @Test
    public void lst() {
        Grammar g;
        Map<Integer, PrefixSet> firsts;
        StringArrayList symbolTable;
        PrefixSet set;
        PrefixSet expected;

        g = Grammar.forProductions("I I a", "I");
        symbolTable = g.getSymbolTable();
        firsts = g.firsts(1);
        assertEquals(2, firsts.size());
        set = firsts.get(symbolTable.indexOf("I"));
        expected = PrefixSet.one();
        expected.addUnpacked(symbolTable.indexOf("a"));
        assertEquals(expected, set);
    }
}
