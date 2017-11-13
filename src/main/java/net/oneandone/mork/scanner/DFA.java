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
package net.oneandone.mork.scanner;

import net.oneandone.mork.regexpr.Range;
import net.oneandone.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.List;

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
        List<Range> ranges;
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
            ranges = new ArrayList<Range>();
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
                        ranges.add((Range) tmp);
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
