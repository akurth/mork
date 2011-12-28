package net.sf.beezle.mork.grammar;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GrammarTest {
    @Test
    public void normal() {
        Grammar g;

        g = Grammar.forProductions("S A B", "A a b", "B x y");
        assertEquals(3, g.getProductionCount());
        assertEquals(1, g.getAlternativeCount(g.getSymbolTable().indexOf("S")));
    }
}
