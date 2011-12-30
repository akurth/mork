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

package net.sf.beezle.mork.lrparser;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LrPDA {
    public static LrPDA create(Grammar grammar) {
        LrPDA pda;
        LrState state;
        IntBitSet nullable;
        Map<Integer, IntBitSet> firsts;

        nullable = new IntBitSet();
        grammar.addNullable(nullable);
        firsts = grammar.firsts(nullable);
        pda = new LrPDA(grammar, grammar.getStart());
        state = LrState.forStartSymbol(0, grammar, grammar.getSymbolCount());
        state.closure(grammar, nullable, firsts);
        pda.states.add(state);
        // size grows!
        for (int i = 0; i < pda.states.size(); i++) {
            state = pda.states.get(i);
            state.gotos(pda, nullable, firsts);
            state.reduces(pda);
        }
        return pda;
    }

    /** start symbol */
    public final int start;

    // environment for computation
    public final Grammar grammar;
    public final List<LrState> states;

    private LrState end;

    //--

    public LrPDA(Grammar grammar, int start) {
        this.grammar = grammar;
        this.states = new ArrayList<LrState>();
        this.start = start;
    }

    //--

    public int size() {
        return states.size();
    }

    public void print(PrintStream dest) {
        int i, max;

        max = states.size();
        for (i = 0; i < max; i++) {
            dest.println(states.get(i).toString(this, grammar));
        }
    }
}
