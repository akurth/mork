package net.sf.beezle.mork.grammar;

import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.sushi.util.IntBitSet;
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
        IntBitSet nullable;
        Map<Integer, PrefixSet> firsts;
        StringArrayList symbolTable;

        g = Grammar.forProductions("Z S",
                "S S b",
                "S b A a",
                "A a S c",
                "A a",
                "A a S b");
        symbolTable = g.getSymbolTable();
        nullable = new IntBitSet();
        g.addNullable(nullable);
        firsts = g.firsts(nullable);
        assertEquals(6, firsts.size());
        assertEquals(PrefixSet.single(symbolTable.indexOf("b")), firsts.get(symbolTable.indexOf("Z")));
        assertEquals(PrefixSet.single(symbolTable.indexOf("b")), firsts.get(symbolTable.indexOf("S")));
        assertEquals(PrefixSet.single(symbolTable.indexOf("a")), firsts.get(symbolTable.indexOf("A")));
    }
}
