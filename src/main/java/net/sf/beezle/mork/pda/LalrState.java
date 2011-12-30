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
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.parser.ParserTable;
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

public class LalrState extends BaseState {
    /** number representing this state in the resulting table. */
    public final int id;

    /** Set of Items. Subset of closure. Sorted in order to speed up equals(). */
    private final SortedSet<LalrItem> core;

    private final List<LalrItem> closure;

    /** List of Shifts. */
    private final List<LalrShift> shifts;

    /** List of Reduces. */
    private final List<LalrReduce> reduces;

    //--

    public static LalrState create(LalrPDA env, int symbol) {
        Collection<LalrItem> coreCol;

        coreCol = new ArrayList<LalrItem>();
        LalrItem.addClosure(env.grammar, symbol, coreCol);
        return new LalrState(env, coreCol);
    }

    public LalrState(LalrPDA env, Collection<LalrItem> coreCol) {
        int i;
        List<LalrItem> todo;
        LalrItem item;

        id = env.states.size();
        shifts = new ArrayList<LalrShift>();
        reduces = new ArrayList<LalrReduce>();

        core = new TreeSet<LalrItem>(coreCol); // adds, sorts and removes duplicates
        todo = new ArrayList<LalrItem>(core); // avoid duplicates - don't use coreCol
        closure = new ArrayList<LalrItem>();

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
        LalrState state;

        if (obj instanceof LalrState) {
            state = (LalrState) obj;
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

    public LalrState createShifted(LalrPDA env, int symbol) {
        LalrState end;

        end = new LalrState(env, Collections.<LalrItem>emptyList());
        shifts.add(new LalrShift(symbol, end));
        env.states.add(end);
        return end;
    }

    /** one step in LR(0) construction */
    public void expand(LalrPDA env) {
        List<LalrItem> remaining;
        LalrItem item;
        List<LalrItem> lst;
        LalrState next;
        int idx;
        Iterator<LalrItem> pos;
        int shift;

        remaining = new ArrayList<LalrItem>(closure);
        while (!remaining.isEmpty()) {
            pos = remaining.iterator();
            item = pos.next();
            pos.remove();
            shift = item.getShift(env.grammar);
            if (shift == -1) {
                reduces.add(new LalrReduce(item.production));
            } else {
                lst = new ArrayList<LalrItem>();
                lst.add(item.createShifted());
                while (pos.hasNext()) {
                    item = pos.next();
                    if (item.getShift(env.grammar) == shift) {
                        pos.remove();
                        lst.add(item.createShifted());
                    }
                }

                next =  new LalrState(env, lst);
                idx = env.states.indexOf(next);
                if (idx != -1) {
                    next = env.states.get(idx);
                } else {
                    env.states.add(next);
                }
                shifts.add(new LalrShift(shift, next));
            }
        }
    }

    //--
    // prepare follow calc

    /** Calculate anything available after LR(0) automaton is complete. */
    public void prepare(LalrPDA env, List<LalrShift> allShifts) {
        int prod, alt, maxAlt;
        LalrState end;
        LalrReduce r;

        for (LalrShift sh : shifts) {
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

    public void addReadInit(LalrPDA env, IntBitSet result) {
        for (LalrShift sh : shifts) {
            if (sh.isEof(env) || !env.grammar.isNonterminal(sh.symbol)) {
                result.add(sh.symbol);
            }
        }
    }

    public void addReadImplies(LalrPDA env, Set<LalrShift> result) {
        for (LalrShift sh : shifts) {
            if (env.nullable.contains(sh.symbol)) {
                result.add(sh);
            }
        }
    }

    //--

    public LalrShift findShift(int symbol) {
        for (LalrShift sh : shifts) {
            if (sh.symbol == symbol) {
                return sh;
            }
        }
        return null;
    }

    public LalrReduce findReduce(int prod) {
        for (LalrReduce r : reduces) {
            if (r.production == prod) {
                return r;
            }
        }
        return null;
    }

    /**
     * @param result  list of Shifts
     */
    public boolean trace(LalrPDA env, int prod, List<LalrShift> result) {
        int ofs, len;
        LalrState state;
        LalrShift t;

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

    public LalrState trace(LalrPDA env, int prod) {
        int ofs, len;
        LalrState state;
        LalrShift t;

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
        int terminal;

        for (LalrShift sh : shifts) {
            result.addShift(id, sh.symbol, sh.end.id, handler);
        }
        for (LalrReduce r : reduces) {
            for (terminal = r.lookahead.first(); terminal != -1; terminal = r.lookahead.next(terminal)) {
                result.addReduce(id, terminal, r.production, handler);
            }
        }
    }

    //--

    public void calcLookahead() {
        for (LalrReduce r : reduces) {
            r.calcLookahead();
        }
    }

    //--

    public String toString(LalrPDA env, Grammar grammar) {
        StringBuilder result;
        StringArrayList symbolTable;

        symbolTable = grammar.getSymbolTable();
        result = new StringBuilder();
        result.append("\n------------------------------\n");
        result.append("[state " + id + "]\n");
        for (LalrItem item : core) {
            result.append(item.toString(env, symbolTable));
        }
        result.append('\n');
        for (LalrItem item : closure) {
            if (!core.contains(item)) {
                result.append(item.toString(env, symbolTable));
            }
        }
        result.append('\n');
        for (LalrShift sh : shifts) {
            result.append(sh.toString(symbolTable));
        }
        result.append('\n');
        for (LalrReduce r : reduces) {
            result.append(r.toString(grammar));
        }
        result.append("\n");
        return result.toString();
    }
}
