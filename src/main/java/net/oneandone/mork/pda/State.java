/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.pda;

import net.oneandone.mork.compiler.ConflictHandler;
import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.grammar.Prefix;
import net.oneandone.mork.grammar.PrefixSet;
import net.oneandone.mork.parser.ParserTable;
import net.oneandone.sushi.util.IntBitSet;

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
            state.items.add(Item.create(grammar, grammar.getAlternative(symbol, alt), PrefixSet.one(eof)));
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

    public void gotos(PDABuilder pda, Map<Integer, PrefixSet> firsts, Queue created, int k) {
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
                    shifted = item.createShifted();
                    if (shifted != null) {
                        state.items.add(shifted);
                    }
                }
            }
            state.closure(grammar, firsts, k);
            target = pda.addIfNew(state);
            if (target < 0) {
                created.put(state);
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

    public void addActions(int id, ParserTable result, ConflictHandler handler) {
        Prefix prefix;

        // shifts first - they cannot cause conflicts
        for (Shift sh : shifts) {
            result.addShift(id, sh.symbol, sh.end);
        }
        for (Item item : items) {
            if (item.isReduce()) {
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

    public Item getReduceItem(int production) {
        for (Item item : items) {
            if (item.isReduce() && (item.getProduction() == production)) {
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
