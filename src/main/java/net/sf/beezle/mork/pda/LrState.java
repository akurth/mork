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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** LR(1) state */

public class LrState extends BaseState<LrShift, LrReduce> {
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

    private final List<LrItem> items;

    public LrState(int id) {
        this(id, new ArrayList<LrItem>());
    }

    public LrState(int id, List<LrItem> items) {
        super(id, new ArrayList<LrShift>(), new ArrayList<LrReduce>());
        this.items = items;
    }

    public List<? extends BaseItem> allItems() {
        return items;
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

    public void gotos(LrPDA pda, IntBitSet nullable, Map<Integer, IntBitSet> firsts, List<LrState> created) {
        IntBitSet shiftSymbols;
        int symbol;
        LrState state;
        LrState target;
        LrItem shifted;

        shiftSymbols = getShiftSymbols(pda.grammar);
        for (symbol = shiftSymbols.first(); symbol != -1; symbol = shiftSymbols.next(symbol)) {
            state = new LrState(pda.size());
            for (LrItem item : items) {
                if (item.getShift(pda.grammar) == symbol) {
                    shifted = item.createShifted(pda.grammar);
                    if (shifted != null) {
                        state.items.add(shifted);
                    }
                }
            }
            state.closure(pda.grammar, nullable, firsts);
            target = pda.addIfNew(state);
            if (target == state) {
                created.add(target);
            }
            this.shifts.add(new LrShift(symbol, target));
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
                reduces.add(new LrReduce(item.production, item.lookahead));
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
        return items.size() == 0 ? 0 : items.get(0).hashCode()  * 256 + items.get(items.size() - 1).hashCode();
    }
}
