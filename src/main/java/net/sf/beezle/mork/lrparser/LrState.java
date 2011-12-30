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
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** LR(1) state */

public class LrState extends BaseState {
    public static LrState forStartSymbol(int id, Grammar grammar, int eof) {
        int symbol;
        LrState state;
        int max;

        state = new LrState(id);
        symbol = grammar.getStart();
        max = grammar.getAlternativeCount(symbol);
        for (int alt = 0; alt < max; alt++) {
            state.items.add(new LrItem(grammar.getAlternative(symbol, alt), 0, IntBitSet.with(eof)));
        }
        return state;
    }

    /** number representing this state in the resulting table. */
    public final int id;

    private final List<LrItem> items;
    private final List<LrShift> shifts;
    private final List<LrReduce> reduces;

    public LrState(int id) {
        this(id, new ArrayList<LrItem>());
    }

    public LrState(int id, List<LrItem> items) {
        this.id = id;
        this.items = items;
        this.shifts = new ArrayList<LrShift>();
        this.reduces = new ArrayList<LrReduce>();
    }

    public void closure(Grammar grammar, IntBitSet nullable, Map<Integer, IntBitSet> firsts) {
        LrItem item;
        LrItem cmp;
        // size grows!
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            item.expanded(grammar, nullable, firsts, items);
        }

        //-- normalize

        // size changes
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            for (int j = i + 1; j < items.size(); j++) {
                cmp = items.get(j);
                if (item.sameCore(cmp)) {
                    item.lookahead.addAll(cmp.lookahead);
                    items.remove(j);
                    j--;
                }
            }
        }
        Collections.sort(items);
    }

    public void gotos(LrPDA pda, IntBitSet nullable, Map<Integer, IntBitSet> firsts) {
        IntBitSet shiftSymbols;
        int symbol;
        LrState state;
        LrItem shifted;
        int id;

        shiftSymbols = getShiftSymbols(pda.grammar);
        for (symbol = shiftSymbols.first(); symbol != -1; symbol = shiftSymbols.next(symbol)) {
            state = new LrState(pda.states.size());
            for (LrItem item : items) {
                if (item.getShift(pda.grammar) == symbol) {
                    shifted = item.createShifted(pda.grammar);
                    if (shifted != null) {
                        state.items.add(shifted);
                    }
                }
            }
            state.closure(pda.grammar, nullable, firsts);
            id = pda.states.indexOf(state);
            if (id == -1) {
                pda.states.add(state);
            } else {
                state = pda.states.get(id);
            }
            this.shifts.add(new LrShift(symbol, state));
        }
    }

    private IntBitSet getShiftSymbols(Grammar grammar) {
        IntBitSet result;
        int symbol;

        result = new IntBitSet();
        for (LrItem item : items) {
            symbol = item.getShift(grammar);
            if (symbol != -1) {
                result.add(symbol);
            }
        }
        return result;
    }

    public void reduces(LrPDA pda) {
        for (LrItem item : items) {
            if (item.getShift(pda.grammar) == -1) {
                reduces.add(new LrReduce(item.production));
            }
        }
    }

    //--

    @Override
    public boolean equals(Object obj) {
        LrState state;

        if (obj instanceof LrState) {
            state = (LrState) obj;
            return items.equals(state.items);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return items.size() == 0 ? 0 : items.get(0).hashCode();
    }

    public String toString(LrPDA env, Grammar grammar) {
        StringBuilder result;
        StringArrayList symbolTable;

        symbolTable = grammar.getSymbolTable();
        result = new StringBuilder();
        result.append("\n------------------------------\n");
        result.append("[state " + id + "]\n");
        for (LrItem item : items) {
            result.append(item.toString(env, symbolTable));
        }
        result.append('\n');
        for (LrShift sh : shifts) {
            result.append(sh.toString(symbolTable));
        }
        result.append('\n');
        for (LrReduce r : reduces) {
            result.append(r.toString(grammar));
        }
        result.append("\n");
        return result.toString();
    }
}
