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
    public static State forStartSymbol(int id, Grammar grammar, int eof, int k) {
        int symbol;
        State state;
        int max;

        state = new State(id);
        symbol = grammar.getStart();
        max = grammar.getAlternativeCount(symbol);
        for (int alt = 0; alt < max; alt++) {
            state.items.add(new Item(grammar.getAlternative(symbol, alt), 0, PrefixSet.one(k, eof)));
        }
        return state;
    }

    public final int id;
    public final List<Shift> shifts;
    private final List<Item> items;

    public State(int id) {
        this(id, new ArrayList<Item>());
    }

    public State(int id, List<Item> items) {
        this.id = id;
        this.items = items;
        this.shifts = new ArrayList<Shift>();
    }

    public void closure(Grammar grammar, Map<Integer, PrefixSet> firsts) {
        Item item;
        Item cmp;

        // size grows!
        for (int i = 0; i < items.size(); i++) {
            item = items.get(i);
            item.expanded(grammar, firsts, items);
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

    public void gotos(PDA pda, Map<Integer, PrefixSet> firsts, List<State> created) {
        IntBitSet shiftSymbols;
        int symbol;
        State state;
        State target;
        Item shifted;

        shiftSymbols = getShiftSymbols(pda.grammar);
        for (symbol = shiftSymbols.first(); symbol != -1; symbol = shiftSymbols.next(symbol)) {
            state = new State(pda.size());
            for (Item item : items) {
                if (item.getShift(pda.grammar) == symbol) {
                    shifted = item.createShifted(pda.grammar);
                    if (shifted != null) {
                        state.items.add(shifted);
                    }
                }
            }
            state.closure(pda.grammar, firsts);
            target = pda.addIfNew(state);
            if (target == state) {
                created.add(target);
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
        return items.size() == 0 ? 0 : items.get(0).hashCode()  * 256 + items.get(items.size() - 1).hashCode();
    }

    public void addActions(Grammar grammar, ParserTable result, ConflictHandler handler) {
        IntBitSet reduceTerminals;
        List<Item> lst;

        // shifts first - they cannot cause conflict
        for (Shift sh : shifts) {
            result.addShift(id, sh.symbol, sh.end.id);
        }
        reduceTerminals = reduceTerminals(grammar);
        for (int terminal = reduceTerminals.first(); terminal != -1; terminal = reduceTerminals.next(terminal)) {
            lst = reduceItems(terminal, grammar);
            // TODO: can I have multiple items for the same production here?
            switch (lst.size()) {
                case 0:
                    throw new IllegalStateException();
                case 1:
                    result.addReduce(id, terminal, lst.get(0).getProduction(), handler);
                    break;
                default:
                    handler.resolve(id, terminal, lst, result);
            }
        }
    }

    private IntBitSet reduceTerminals(Grammar grammar) {
        IntBitSet result;

        result = new IntBitSet();
        for (Item item : items) {
            if (item.getShift(grammar) == -1) {
                for (Prefix prefix : item.lookahead) {
                    if (prefix.size() < 1) {
                        throw new IllegalStateException();
                    }
                    result.add(prefix.get(0));
                }
            }
        }
        return result;
    }

    private List<Item> reduceItems(int terminal, Grammar grammar) {
        List<Item> result;

        result = new ArrayList<Item>();
        for (Item item : items) {
            if (item.getShift(grammar) == -1) {
                for (Prefix prefix : item.lookahead) {
                    if (prefix.get(0) == terminal) {
                        result.add(item);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static boolean containsProd(List<Item> all, int prod) {
        for (Item item : all) {
            if (item.getProduction() == prod) {
                return true;
            }
        }
        return false;
    }

    public Shift lookupShift(int symbol) {
        for (Shift shift : shifts) {
            if (shift.symbol == symbol) {
                return shift;
            }
        }
        return null;
    }

    public String toString(Grammar grammar) {
        StringBuilder result;

        result = new StringBuilder();
        result.append("\n------------------------------\n");
        result.append("[state " + id + "]\n");
        for (Item item : items) {
            result.append(item.toString(grammar));
        }
        result.append('\n');
        for (Shift sh : shifts) {
            result.append(sh.toString(grammar.getSymbolTable()));
        }
        result.append('\n');
        return result.toString();
    }
}
