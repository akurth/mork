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

import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* LR(1) automaton, follow the description in http://amor.cms.hu-berlin.de/~kunert/papers/lr-analyse/ */

public class PDA implements Iterable<State> {
    protected final Grammar grammar;
    private final HashMap<State, State> states;
    protected State start;

    public static PDA create(Grammar grammar) {
        PDA pda;
        State state;
        IntBitSet nullable;
        Map<Integer, IntBitSet> firsts;
        List<State> todo;

        nullable = new IntBitSet();
        grammar.addNullable(nullable);
        firsts = grammar.firsts(nullable);
        pda = new PDA(grammar);
        state = State.forStartSymbol(0, grammar, grammar.getSymbolCount());
        state.closure(grammar, nullable, firsts);
        pda.add(state);
        todo = new ArrayList<State>();
        todo.add(state);
        // size grows!
        for (int i = 0; i < todo.size(); i++) {
            state = todo.get(i);
            state.gotos(pda, nullable, firsts, todo);
            state.reduces(pda);
        }

        // TODO: hack hack hack
        state = new State(pda.size());
        pda.add(state);
        pda.start.shifts.add(new Shift(grammar.getStart(), state));
        return pda;
    }

    public PDA(Grammar grammar) {
        this.grammar = grammar;
        this.states = new HashMap<State, State>();
        this.start = null;
    }

    public Iterator<State> iterator() {
        return states.keySet().iterator();
    }

    public void add(State state) {
        if (start == null) {
            start = state;
        }
        states.put(state, state);
    }

    public State addIfNew(State state) {
        State existing;

        existing = states.get(state);
        if (existing == null) {
            add(state);
            return state;
        }
        return existing;
    }

    public int size() {
        return states.size();
    }

    /**
     * Pseudo-symbol, indicates end-of-file (or an empty word if lookahead is seen as a set of words of length <= 1)
     */
    public int getEofSymbol() {
        return grammar.getSymbolCount();
    }

    public void print(PrintStream dest) {
        for (State state : this) {
            dest.println(state.toString(grammar));
        }
    }

    public ParserTable createTable(int lastSymbol, ConflictHandler handler) throws GenericException {
        // the initial syntaxnode created by the start action is ignoed!
        ParserTable result;
        int eof;
        State end;

        eof = getEofSymbol();
        result = new ParserTable(0, size(), lastSymbol + 1 /* +1 for EOF */, eof, grammar, null);
        for (State state : this) {
            state.addActions(result, handler);
        }
        end = start.lookupShift(grammar.getStart()).end;
        result.addAccept(end.id, eof);
        return result;
    }
}
