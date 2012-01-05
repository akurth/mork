package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.grammar.Grammar;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PDATest {
    @Test
    public void simple() {
        check(2, 1, "S a");
    }

    @Test
    public void g1() {
        check(13, 1,
                "Z S",
                "S S b",
                "S b A a",
                "A a S c",
                "A a",
                "A a S b");
    }

    @Test
    public void lr2() {
        PDA pda;

        pda = check(8, 2,
                "Z S",
                "S Y a a",
                "S X a",
                "X b",
                "Y b"
                );
        pda.print(System.out);
    }

    private PDA check(int states, int k, String ... prods) {
        Grammar grammar;
        PDA pda;

        grammar = Grammar.forProductions(prods);
        pda = PDA.create(grammar, k);
        // pda.print(System.out);
        assertEquals(states, pda.size() - 1 /* TODO: artificial end state */);
        return pda;
    }
}
