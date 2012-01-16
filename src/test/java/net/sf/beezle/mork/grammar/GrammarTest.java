package net.sf.beezle.mork.grammar;

import net.sf.beezle.mork.misc.StringArrayList;
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
        expected = PrefixSet.zero();
        expected.add(Prefix.forSymbol(symbolTable.indexOf("a")).data);
        assertEquals(expected, set);
    }
}
