package net.sf.beezle.mork.lrparser;

import net.sf.beezle.mork.grammar.Grammar;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PDATest {
    @Test
    public void simple() {
        check(2, "S a");
    }

    @Test
    public void g1() {
        check(13,
                "Z S",
                "S S b",
                "S b A a",
                "A a S c",
                "A a",
                "A a S b");
    }

    private void check(int states, String ... prods) {
        Grammar grammar;
        LrPDA pda;

        grammar = Grammar.forProductions(prods);
        pda = LrPDA.create(grammar);
        assertEquals(states, pda.states.size());
    }
}
