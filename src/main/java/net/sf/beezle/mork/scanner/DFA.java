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

package net.sf.beezle.mork.scanner;

import java.util.ArrayList;
import java.util.List;

import net.sf.beezle.sushi.util.IntBitSet;

import net.sf.beezle.mork.regexpr.Range;

/** Create determinitic automatons. */

public class DFA {
    /**
     * Factory method to create an deterministic automaton.
     * The standard algorithm is used. SableCC 2.6 and JFlex 1.1.2 use the
     * same algorithm. A description can be found, e.g., in [Aho96]
     * @return a deterministic FA.
     */

    public static FA create(FA nfa) {
        if (nfa.getStart() == -1) {
            throw new IllegalArgumentException();
        }

        FA dfa;                // result
        int dfaIdx, nextDfaIdx, nfaIdx;
        int transition, max;
        State dfaState;       // current state
        State nfaState;
        IntBitSet closure;       // all nfa state represented by current state
        IntBitSet nextClosure;
        IntBitSet[] epsilonClosures;

        Object tmp;
        int i, rangeCount;
        List ranges;
        Range small, large;

        epsilonClosures = nfa.epsilonClosures();
        closure = new IntBitSet();
        closure.add(nfa.getStart());
        closure.addAllSets(epsilonClosures);
        dfa = new FA();
        dfa.setStart(dfa.add(closure));

        // dfa grows during the loop
        for (dfaIdx = 0; dfaIdx < dfa.size(); dfaIdx++) {

            // state under construction
            dfaState = dfa.get(dfaIdx);

            closure = (IntBitSet) dfaState.getLabel();

            // collect all possible transitions;
            // mark end states
            ranges = new ArrayList();
            for (nfaIdx = closure.first(); nfaIdx != -1;
                 nfaIdx = closure.next(nfaIdx)) {

                if (nfa.isEnd(nfaIdx)) {
                    dfa.setEnd(dfaIdx);
                }

                nfaState = nfa.get(nfaIdx);
                max = nfaState.size();
                for (transition = 0; transition < max; transition++) {
                    tmp = nfaState.getInput(transition);
                    if (tmp != null) {
                        ranges.add(tmp);
                    }
                }
            }

            Range.normalizeRanges(ranges);

            rangeCount = ranges.size();
            for (i = 0; i < rangeCount; i++) {
                small = (Range) ranges.get(i);
                nextClosure = new IntBitSet();
                for (nfaIdx = closure.first(); nfaIdx != -1;
                     nfaIdx = closure.next(nfaIdx)) {

                    nfaState = nfa.get(nfaIdx);
                    max = nfaState.size();
                    for (transition = 0; transition < max; transition++) {
                        large = (Range) nfaState.getInput(transition);
                        if ((large != null) && large.contains(small)) {
                            nextClosure.add(nfaState.getEnd(transition));
                        }
                    }
                }
                nextClosure.addAllSets(epsilonClosures);
                nextDfaIdx = dfa.find(nextClosure);
                if (nextDfaIdx == -1) {
                    nextDfaIdx = dfa.add(nextClosure);
                }

                dfaState.add(nextDfaIdx, small);
            }
        }

        Label.combineLabels(dfa, nfa);

        return dfa;
    }
}
