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
import net.sf.beezle.mork.grammar.Prefix;
import net.sf.beezle.mork.grammar.PrefixSet;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** LR(k) state */

public class State {
    public static State forStartSymbol(Grammar grammar, int eof) {
        int symbol;
        State state;
        int max;

        state = new State();
        symbol = grammar.getStart();
        max = grammar.getAlternativeCount(symbol);
        for (int alt = 0; alt < max; alt++) {
            state.items.add(new Item(grammar.getAlternative(symbol, alt), 0, PrefixSet.one(eof)));
        }
        return state;
    }

    public final List<Shift> shifts;
    public final List<Item> items;

    public State() {
        this.items = new ArrayList<Item>();
        this.shifts = new ArrayList<Shift>();
    }

    public void closure(Grammar grammar, Map<Integer, PrefixSet> firsts, int k) {
        Item item;
        Item cmp;

        // size grows!
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            item.expanded(grammar, firsts, items, k);
        }

        //-- normalize

        // size shrinks
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            for (int j = i + 1; j < items.size(); j++) {
                cmp = items.get(j);
                if (item.core == cmp.core) {
                    item.lookahead.addAll(cmp.lookahead);
                    items.remove(j);
                    j--;
                }
            }
        }
        Collections.sort(items);
    }

    public void gotos(PDABuilder pda, Map<Integer, PrefixSet> firsts, List<State> created, int k) {
        Grammar grammar;
        IntBitSet shiftSymbols;
        int symbol;
        State state;
        int target;
        Item shifted;

        grammar = pda.getGrammar();
        shiftSymbols = getShiftSymbols(grammar);
        for (symbol = shiftSymbols.first(); symbol != -1; symbol = shiftSymbols.next(symbol)) {
            state = new State();
            for (Item item : items) {
                if (item.getShift(grammar) == symbol) {
                    shifted = item.createShifted(grammar);
                    if (shifted != null) {
                        state.items.add(shifted);
                    }
                }
            }
            state.closure(grammar, firsts, k);
            target = pda.addIfNew(state);
            if (target < 0) {
                created.add(state);
                target = -target;
            }
            this.shifts.add(new Shift(symbol, target));
        }
    }

    private IntBitSet getShiftSymbols(Grammar grammar) {
        IntBitSet result;
        int symbol;

        result = new IntBitSet();
        for (Item item : items) {
            symbol = item.getShift(grammar);
            if (symbol != -1) {
                result.add(symbol);
            }
        }
        return result;
    }

    //--

    @Override
    public boolean equals(Object obj) {
        State state;

        if (obj instanceof State) {
            state = (State) obj;
            return items.equals(state.items);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return items.size() == 0 ? 0 : items.get(0).hashCode() * 256 + items.get(items.size() - 1).hashCode();
    }

    public void addActions(int id, Grammar grammar, ParserTable result, ConflictHandler handler) {
        Prefix prefix;

        // shifts first - they cannot cause conflicts
        for (Shift sh : shifts) {
            result.addShift(id, sh.symbol, sh.end);
        }
        for (Item item : items) {
            if (item.getShift(grammar) == -1) {
                prefix = item.lookahead.iterator();
                while (prefix.step()) {
                    if (prefix.size() < 1) {
                        throw new IllegalStateException();
                    }
                    result.addReduce(id, this, prefix.first(), item.getProduction(), handler);
                }
            }
        }
    }

    public Shift lookupShift(int symbol) {
        for (Shift shift : shifts) {
            if (shift.symbol == symbol) {
                return shift;
            }
        }
        return null;
    }

    public Item getReduceItem(Grammar grammar, int production) {
        for (Item item : items) {
            if ((item.getProduction() == production) && (item.getShift(grammar) == -1)) {
                return item;
            }
        }
        throw new IllegalStateException();
    }
    
    public String toString(int id, Grammar grammar) {
        StringBuilder result;

        result = new StringBuilder();
        result.append("\n------------------------------\n");
        result.append("[state " + id + "]\n");
        for (Item item : items) {
            result.append(item.toString(grammar, false));
        }
        result.append('\n');
        for (Shift sh : shifts) {
            result.append(sh.toString(grammar.getSymbolTable()));
        }
        result.append('\n');
        return result.toString();
    }

    public String toShortString(Grammar grammar) {
        StringBuilder result;

        result = new StringBuilder();
        for (Item item : items) {
            result.append(item.toString(grammar, true));
        }
        result.append('\n');
        for (Shift sh : shifts) {
            result.append(sh.toString(grammar.getSymbolTable()));
        }
        result.append('\n');
        return result.toString();
    }
}
