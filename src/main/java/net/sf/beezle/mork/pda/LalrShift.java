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

import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LalrShift extends BaseShift {
    private final IntBitSet readInit;     // DR: directly reads
    private final Set<LalrShift> readImplies;       // READS: set of Shifts
    private IntBitSet read;         // read

    // read is followImplies
    private final Set<LalrShift> followImplies;    // INCLUDES: set of Shifts
    private IntBitSet follow;      // follow

    public LalrShift(int symbol, LalrState end) {
        super(symbol, end);

        readInit = new IntBitSet();
        readImplies = new HashSet<LalrShift>();
        read = new IntBitSet();
        followImplies = new HashSet<LalrShift>();
        follow = new IntBitSet();
    }

    //--

    /** calculate anything available when LR(0) is implete. */

    public void prepare(LalrPDA env, LalrState start) {
        int prod, alt, maxAlt;
        int i;
        List<LalrShift> lst;
        LalrShift t;

        // read implies
        ((LalrState) end).addReadImplies(env, readImplies);

        if (!isEof(env) && env.grammar.isNonterminal(symbol)) {
            // read init
            ((LalrState) end).addReadInit(env, readInit);

            // follow implies
            maxAlt = env.grammar.getAlternativeCount(symbol);
            for (alt = 0; alt < maxAlt; alt++) {
                prod = env.grammar.getAlternative(symbol, alt);
                lst = new ArrayList<LalrShift>();
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

    public boolean isEof(LalrPDA pda) {
        return symbol == pda.getEofSymbol();
    }


    //--

    // for closure computation
    private IntBitSet clInit;
    private Set<LalrShift> clImplies;
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
    public void digraph(List<LalrShift> stack) {
        if (clN == 0) {
            traverse(stack);
        }
    }

    private void traverse(List<LalrShift> stack) {
        int d;  // initial stack size
        LalrShift s;

        // initialize
        stack.add(this);
        d = stack.size();
        clN = d;
        clResult.addAll(clInit);

        // complete closure process
        for (LalrShift t : clImplies) {
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
}
