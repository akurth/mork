package net.sf.beezle.mork.pda;

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
        PDA pda;

        grammar = Grammar.forProductions(prods);
        pda = PDA.create(grammar);
        // pda.print(System.out);
        assertEquals(states, pda.size() - 1 /* TODO: artificial end state */);
    }
}
