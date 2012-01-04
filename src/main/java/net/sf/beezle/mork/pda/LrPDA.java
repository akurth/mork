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

package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* LR(1) automaton, follow the description in http://amor.cms.hu-berlin.de/~kunert/papers/lr-analyse/ */

public class LrPDA extends BasePDA {
    public static LrPDA create(Grammar grammar) {
        LrPDA pda;
        LrState state;
        IntBitSet nullable;
        Map<Integer, IntBitSet> firsts;
        List<LrState> todo;

        nullable = new IntBitSet();
        grammar.addNullable(nullable);
        firsts = grammar.firsts(nullable);
        pda = new LrPDA(grammar);
        state = LrState.forStartSymbol(0, grammar, grammar.getSymbolCount());
        state.closure(grammar, nullable, firsts);
        pda.add(state);
        todo = new ArrayList<LrState>();
        todo.add(state);
        // size grows!
        for (int i = 0; i < todo.size(); i++) {
            state = todo.get(i);
            state.gotos(pda, nullable, firsts, todo);
            state.reduces(pda);
        }

        // TODO: hack hack hack
        state = new LrState(pda.size());
        pda.add(state);
        pda.start.shifts.add(new LrShift(grammar.getStart(), state));
        return pda;
    }

    public LrPDA(Grammar grammar) {
        super(grammar);
    }
}
