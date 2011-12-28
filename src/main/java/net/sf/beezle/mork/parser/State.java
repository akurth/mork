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

package net.sf.beezle.mork.parser;

import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/** LR-PDAs are generated using these states */

public class State {
    /** number representing this state in the resulting table. */
    public final int id;

    /** Set of Items. Subset of closure. Sorted in order to speed up equals(). */
    private final SortedSet<Item> core;

    private final List<Item> closure;

    /** List of Shifts. */
    private final List<Shift> shifts;

    /** List of Reduces. */
    private final List<Reduce> reduces;

    //--

    public static State create(PDA env, int symbol) {
        Collection<Item> coreCol;

        coreCol = new ArrayList<Item>();
        Item.addClosure(env.grammar, symbol, coreCol);
        return new State(env, coreCol);
    }

    public State(PDA env, Collection<Item> coreCol) {
        int i;
        List<Item> todo;
        Item item;

        id = env.states.size();
        shifts = new ArrayList<Shift>();
        reduces = new ArrayList<Reduce>();

        core = new TreeSet<Item>(coreCol); // adds, sorts and removes duplicates
        todo = new ArrayList<Item>(core); // avoid duplicates - don't use coreCol
        closure = new ArrayList<Item>();

        // start loop with empty closure
        // note: loop grows its upper bound
        for (i = 0; i < todo.size(); i++) {
            item = todo.get(i);
            if (closure.contains(item)) {
                // item is already known: do nothing
            } else {
                closure.add(item);
                item.addClosure(env.grammar, todo);
            }
        }
    }

    //--

    @Override
    public boolean equals(Object obj) {
        State state;

        if (obj instanceof State) {
            state = (State) obj;
            return core.equals(state.core);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return core.size();
    }

    //--

    public State createShifted(PDA env, int symbol) {
        State end;

        end = new State(env, Collections.<Item>emptyList());
        shifts.add(new Shift(symbol, end));
        env.states.add(end);
        return end;
    }

    /** one step in LR(0) construction */
    public void expand(PDA env) {
        List<Item> remaining;
        Item item;
        List<Item> lst;
        State next;
        int idx;
        Iterator<Item> pos;
        int shift;

        remaining = new ArrayList<Item>(closure);
        while (!remaining.isEmpty()) {
            pos = remaining.iterator();
            item = pos.next();
            pos.remove();
            shift = item.getShift(env.grammar);
            if (shift == -1) {
                reduces.add(new Reduce(item.production));
            } else {
                lst = new ArrayList<Item>();
                lst.add(item.createShifted());
                while (pos.hasNext()) {
                    item = pos.next();
                    if (item.getShift(env.grammar) == shift) {
                        pos.remove();
                        lst.add(item.createShifted());
                    }
                }

                next =  new State(env, lst);
                idx = env.states.indexOf(next);
                if (idx != -1) {
                    next = env.states.get(idx);
                } else {
                    env.states.add(next);
                }
                shifts.add(new Shift(shift, next));
            }
        }
    }

    //--
    // prepare follow calc

    /** Calculate anything available after LR(0) automaton is complete. */
    public void prepare(PDA env, List<Shift> allShifts) {
        int prod, alt, maxAlt;
        State end;
        Reduce r;

        for (Shift sh : shifts) {
            sh.prepare(env, this);

            allShifts.add(sh);

            if (!sh.isEof(env) && env.grammar.isNonterminal(sh.symbol)) {
                maxAlt = env.grammar.getAlternativeCount(sh.symbol);
                for (alt = 0; alt < maxAlt; alt++) {
                    prod = env.grammar.getAlternative(sh.symbol, alt);
                    end = trace(env, prod);
                    if (env != null) {
                        r = end.findReduce(prod);
                        if (r != null) {
                            r.lookback.add(sh);
                        }
                    }
                }
            }
        }
    }

    public void addReadInit(PDA env, IntBitSet result) {
        for (Shift sh : shifts) {
            if (sh.isEof(env) || !env.grammar.isNonterminal(sh.symbol)) {
                result.add(sh.symbol);
            }
        }
    }

    public void addReadImplies(PDA env, Set<Shift> result) {
        for (Shift sh : shifts) {
            if (env.nullable.contains(sh.symbol)) {
                result.add(sh);
            }
        }
    }

    //--

    public Shift findShift(int symbol) {
        for (Shift sh : shifts) {
            if (sh.symbol == symbol) {
                return sh;
            }
        }
        return null;
    }

    public Reduce findReduce(int prod) {
        for (Reduce r : reduces) {
            if (r.production == prod) {
                return r;
            }
        }
        return null;
    }

    /**
     * @param result  list of Shifts
     */
    public boolean trace(PDA env, int prod, List<Shift> result) {
        int ofs, len;
        State state;
        Shift t;

        state = this;
        len = env.grammar.getLength(prod);
        for (ofs = 0; ofs < len; ofs++) {
            t = state.findShift(env.grammar.getRight(prod, ofs));
            if (t == null) {
                return false;
            }
            result.add(t);
            state = t.end;
        }
        return true;
    }

    public State trace(PDA env, int prod) {
        int ofs, len;
        State state;
        Shift t;

        state = this;
        len = env.grammar.getLength(prod);
        for (ofs = 0; ofs < len; ofs++) {
            t = state.findShift(env.grammar.getRight(prod, ofs));
            if (t == null) {
                return null;
            }
            state = t.end;
        }
        return state;
    }

    //--

    public void addActions(ParserTable result, ConflictHandler handler) {
        Iterator<Shift> p1;
        Iterator<Reduce> p2;
        Shift sh;
        Reduce r;
        int term;

        p1 = shifts.iterator();
        while (p1.hasNext()) {
            sh = p1.next();
            result.addShift(id, sh.symbol, sh.end.id, handler);
        }
        p2 = reduces.iterator();
        while (p2.hasNext()) {
            r = p2.next();
            for (term = r.lookahead.first(); term != -1; term = r.lookahead.next(term)) {
                result.addReduce(id, term, r.production, handler);
            }
        }
    }

    //--

    public void calcLookahead() {
        for (Reduce r : reduces) {
            r.calcLookahead();
        }
    }

    //--

    public String toString(PDA env, Grammar grammar) {
        StringBuilder result;
        StringArrayList symbolTable;

        symbolTable = grammar.getSymbolTable();
        result = new StringBuilder();
        result.append("\n------------------------------\n");
        result.append("[state " + id + "]\n");
        for (Item item : core) {
            result.append(item.toString(env, symbolTable));
        }
        result.append('\n');
        for (Item item : closure) {
            if (!core.contains(item)) {
                result.append(item.toString(env, symbolTable));
            }
        }
        result.append('\n');
        for (Shift sh : shifts) {
            result.append(sh.toString(symbolTable));
        }
        result.append('\n');
        for (Reduce r : reduces) {
            result.append(r.toString(grammar));
        }
        result.append("\n");
        return result.toString();
    }
}
