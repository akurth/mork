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
import net.sf.beezle.mork.grammar.PrefixSet;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.ParserTable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* LR(k) automaton, follow the description in http://amor.cms.hu-berlin.de/~kunert/papers/lr-analyse/ */

public class PDA {
    public static PDA create(Grammar grammar, Map<Integer, PrefixSet> firsts, int k) {
        PDA pda;
        State state;
        List<State> todo;
        int end;

        state = State.forStartSymbol(grammar, grammar.getSymbolCount());
        state.closure(grammar, firsts, k);
        pda = new PDA(grammar, state);
        todo = new ArrayList<State>();
        todo.add(state);
        // size grows!
        for (int i = 0; i < todo.size(); i++) {
            state = todo.get(i);
            state.gotos(pda, firsts, todo, k);
        }

        // TODO: hack hack hack
        end = pda.add(new State());
        pda.start.shifts.add(new Shift(grammar.getStart(), end));
        return pda;
    }

    public final Grammar grammar;
    private final HashMap<State, Integer> states;
    private final State start;

    public PDA(Grammar grammar, State start) {
        this.grammar = grammar;
        this.states = new LinkedHashMap<State, Integer>();
        this.start = start;
        add(start);
    }

    private Iterable<State> states() {
        return states.keySet();
    }

    public int add(State state) {
        int id;
        
        id = states.size();
        states.put(state, id);
        return id;
    }

    /** @return id of existing state, -id of newly added state */
    public int addIfNew(State state) {
        Integer existing;

        existing = states.get(state);
        return existing == null ? -add(state) : existing.intValue();
    }

    /** This method is expensive! */
    public State get(int id) {
        for (Map.Entry<State, Integer> entry : states.entrySet()) {
            if (id == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("" + id);
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

    public ParserTable createTable(int lastSymbol, ConflictHandler handler) throws GenericException {
        // the initial syntax node created by the start action is ignored!
        ParserTable result;
        int eof;
        int end;

        eof = getEofSymbol();
        result = new ParserTable(0, size(), lastSymbol + 1 /* +1 for EOF */, eof, grammar, null);
        for (Map.Entry<State, Integer> entry : states.entrySet()) {
            entry.getKey().addActions(entry.getValue(), grammar, result, handler);
        }
        end = start.lookupShift(grammar.getStart()).end;
        result.addAccept(end, eof);
        return result;
    }

    public void print(PrintStream dest) {
        for (Map.Entry<State, Integer> entry : states.entrySet()) {
            dest.println(entry.getKey().toString(entry.getValue(), grammar));
        }
    }

    public void statistics(PrintStream output) {
        int size;
        int itemsCount;
        int itemsMin;
        int itemsMax;
        int lookaheadSizes;
        int lookaheadMin;
        int lookaheadMax;
        double hqMax;
        double hqMin;
        double hqSum;
        double loadMax;
        double loadMin;
        double loadSum;
        double q;

        itemsCount = 0;
        itemsMin = Integer.MAX_VALUE;
        itemsMax = Integer.MIN_VALUE;
        lookaheadMax = Integer.MIN_VALUE;
        lookaheadMin = Integer.MAX_VALUE;
        lookaheadSizes = 0;
        hqSum = 0;
        hqMax = Double.MIN_VALUE;
        hqMin = Double.MAX_VALUE;
        loadSum = 0;
        loadMax = Double.MIN_VALUE;
        loadMin = Double.MAX_VALUE;
        for (State state : states()) {
            size = state.items.size();
            itemsCount += size;
            itemsMin = Math.min(itemsMin, size);
            itemsMax = Math.max(itemsMax, size);
            for (Item item : state.items) {
                size = item.lookahead.size();
                lookaheadSizes += size;
                lookaheadMin = Math.min(lookaheadMin, size);
                lookaheadMax = Math.max(lookaheadMax, size);
                q = item.lookahead.hashQuality();
                hqSum += q;
                hqMax = Math.max(q, hqMax);
                hqMin = Math.min(q, hqMin);
                q = item.lookahead.load();
                loadSum += q;
                loadMax = Math.max(q, loadMax);
                loadMin = Math.min(q, loadMin);
            }
        }
        output.println("parser generation statistics");
        output.println("  states: " + states.size());
        output.println("  items avg: " + (itemsCount / states.size()) + ", min: " + itemsMin + ", max: " + itemsMax + ")");
        output.println("  lookahead avg: " + (lookaheadSizes / itemsCount) + ", min: " + lookaheadMin + ", max: " + lookaheadMax);
        output.println("  hash quality avg: " + (hqSum / itemsCount) + ", min: " + hqMin + ", max: " + hqMax);
        output.println("  hash load avg: " + (loadSum / itemsCount) + ", min: " + loadMin + ", max: " + loadMax);
        output.println("  heap size: " + Runtime.getRuntime().totalMemory());
        output.println("  heap used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }
}
