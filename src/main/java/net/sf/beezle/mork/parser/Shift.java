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

import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Shift {
    /** symbol or eof */
    public final int symbol;
    public final State end;

    private final IntBitSet readInit;     // DR: directly reads
    private final Set<Shift> readImplies;       // READS: set of Shifts
    private IntBitSet read;         // read

    // read is followImplies
    private final Set<Shift> followImplies;    // INCLUDES: set of Shifts
    private IntBitSet follow;      // follow

    public Shift(int symbol, State end) {
        this.symbol = symbol;
        this.end = end;

        readInit = new IntBitSet();
        readImplies = new HashSet<Shift>();
        read = new IntBitSet();
        followImplies = new HashSet<Shift>();
        follow = new IntBitSet();
    }

    //--

    /** calculate anything available when LR(0) is implete. */

    public void prepare(PDA env, State start) {
        int prod, alt, maxAlt;
        int i;
        List<Shift> lst;
        Shift t;

        // read implies
        end.addReadImplies(env, readImplies);

        if (!isEof(env) && env.grammar.isNonterminal(symbol)) {
            // read init
            end.addReadInit(env, readInit);

            // follow implies
            maxAlt = env.grammar.getAlternativeCount(symbol);
            for (alt = 0; alt < maxAlt; alt++) {
                prod = env.grammar.getAlternative(symbol, alt);
                lst = new ArrayList<Shift>();
                if (start.trace(env, prod, lst)) {
                    for (i = lst.size() - 1; i >= 0; i--) {
                        t = lst.get(i);
                        t.followImplies.add(this);
                        if (!env.nullable.contains(t.symbol)) {
                            break;
                        }
                    }
                }
            }
        } else {
            // read init: do nothing
            // follow implies: do nothing
        }
    }

    public boolean isEof(PDA pda) {
        return symbol == pda.getEofSymbol();
    }


    //--

    // for closure computation
    private IntBitSet clInit;
    private Set<Shift> clImplies;
    private IntBitSet clResult;
    private int clN;

    public void initReadCalc() {
        clInit = readInit;
        clImplies = readImplies;
        clResult = new IntBitSet();
        clN = 0;
    }
    public void saveReadCalc() {
        read = clResult;
        clResult = null;
    }
    public void initFollowCalc() {
        clInit = read;
        clImplies = followImplies;
        clResult = new IntBitSet();
        clN = 0;
    }
    public void saveFollowCalc() {
        follow = clResult;
        clResult = null;
    }

    public void addFollow(IntBitSet result) {
        result.addAll(follow);
    }

    /**
     * @param stack of Shifts
     */
    public void digraph(List<Shift> stack) {
        if (clN == 0) {
            traverse(stack);
        }
    }

    private void traverse(List<Shift> stack) {
        int d;  // initial stack size
        Shift s;

        // initialize
        stack.add(this);
        d = stack.size();
        clN = d;
        clResult.addAll(clInit);

        // complete closure process
        for (Shift t : clImplies) {
            if (t.clN == 0) {
                t.traverse(stack);
            }
            clN = Math.min(clN, t.clN);
            clResult.addAll(t.clResult);
        }
        if (clN == d) {
            do {
                s = stack.get(stack.size() - 1);
                stack.remove(stack.size() - 1);
                s.clN = Integer.MAX_VALUE;
                s.clResult.addAll(clResult);
            } while (s != this);
        }
    }

    //--

    public String toString(StringArrayList symbolTable) {
        StringBuilder result;

        result = new StringBuilder();
        result.append("shift ");
        result.append(symbolTable.getOrIndex(symbol));
        result.append(" -> " + end.id + '\n');
        return result.toString();
    }

    @Override
    public int hashCode() {
        return symbol;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
