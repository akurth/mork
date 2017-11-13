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
import net.oneandone.sushi.util.IntArrayList;
import net.oneandone.sushi.util.IntBitSet;

/**
 * Minimization of a finite automaton. Requires a complete depterministic
 * automaton. Algorithm is taken from [Hopcraft80]. SableCC 2.7 and JFlex 1.1.2
 * use the same algorithm. State partitioning as descibed is most compiler text
 * books is to slow.
 */

public class Minimizer {
    private static final IntArrayList YES = new IntArrayList();
    private static final IntArrayList NO = new IntArrayList();

    /** UNKNOWN has to be the default initialization for reference-type array elements: null */
    private static final IntArrayList UNKNOWN = null;

    private final FA fa;

    /** fa.size(); */
    private final int size;

    /**
     * indexed by [leftSi][rightSi]
     * inv: leftSi > rightSi
     * YES: [leftSi][rightSi] is distinct
     * NO:  [leftSi][rightSi} not distinct
     * UNKNOWN: unknown
     * else: distinct is unknown; if it turns out to be distinct, all pairs in the list are distinct
     * @inv distinct[si].length = si
     */
    private final IntArrayList[][] distinct;

    /** old2new[faSi] = resultSi.  assigned by collect */
    private final int[] old2new;

    /**
     * Requires a complete deterministic automaton
     */
    public Minimizer(FA fa) {
        int leftSi, rightSi;

        this.fa = fa;
        size = fa.size();
        old2new = new int[size];
        distinct = new IntArrayList[size][];
        for (leftSi = 0; leftSi < size; leftSi++) {
            distinct[leftSi] = new IntArrayList[leftSi];
            // initialized to null. That takes an extra test during minimization, but
            // it saves a huge amount of memory
        }
        for (leftSi = fa.getFirstEnd(); leftSi != -1; leftSi = fa.getNextEnd(leftSi)) {
            for (rightSi = 0; rightSi < leftSi; rightSi++) {
                if (!fa.isEnd(rightSi) || !Label.sameSymbols(fa, leftSi, rightSi)) {
                    distinct[leftSi][rightSi] = YES;
                }
            }
            for (rightSi = leftSi + 1; rightSi < size; rightSi++) {
                if (!fa.isEnd(rightSi) || !Label.sameSymbols(fa, leftSi, rightSi)) {
                    distinct[rightSi][leftSi] = YES;
                }
            }
        }
    }

    public int getNewSi(int si) {
        return old2new[si];
    }

    /**
     * The caller has to ensure a complete FA.
     * @throws IllegalArgumentException if faInit is not complete.
     */
    public FA run() {
        distinguish();
        return collect();
    }

    private FA collect() {
        int leftSi, rightSi;
        int faSi, resultSi;
        FA result;
        IntBitSet states; // old states
        int resultSize;
        int ti, maxTi;

        result = new FA();
        for (leftSi = 0; leftSi < size; leftSi++) {
            states = new IntBitSet();
            for (rightSi = 0; rightSi < leftSi; rightSi++) {
                if (distinct[leftSi][rightSi] != YES) {
                    // distinct may well be unknown -- this happens if some other state switches
                    // to "NO", depending states are not notified
                    states.add(rightSi);
                }
            }
            // leftSi == rightSi  => distinct == NO
            states.add(rightSi++);
            for (; rightSi < size; rightSi++) {
                if (distinct[rightSi][leftSi] != YES) {
                    // distinct may well be unknown -- this happens if some other state switches
                    // to "NO", depending states are not notified
                    states.add(rightSi);
                }
            }

            if (result.find(states) == -1) {
                resultSi = result.add(states);
                for (faSi = states.first(); faSi != -1; faSi = states.next(faSi)) {
                    old2new[faSi] = resultSi;
                }
            }
        }

        resultSize = result.size();
        for (resultSi = 0; resultSi < resultSize; resultSi++) {
            states = (IntBitSet) result.get(resultSi).getLabel();
            faSi = states.first();
            if (faSi == -1) {
                throw new RuntimeException();
            }

            if (fa.getStart() == faSi) {
                result.setStart(resultSi);
            }
            if (fa.isEnd(faSi)) {
                result.setEnd(resultSi);
            }

            maxTi = fa.get(faSi).size();
            for (ti = 0; ti < maxTi; ti++) {
                result.get(resultSi).add(
                    old2new[fa.get(faSi).getEnd(ti)], (Range) fa.get(faSi).getInput(ti));
            }
        }

        Label.combineLabels(result, fa);

        return result;
    }

    private void distinguish() {
        int leftSi, rightSi;
        IntArrayList d;

        // I tested the four combinations of running the following loop upwards or downwards:
        // the differences are small, and the following seems best:
        for (leftSi = 0; leftSi < size; leftSi++) {
            for (rightSi = 0; rightSi < leftSi; rightSi++) {
                d = distinct[leftSi][rightSi];
                if (d == YES) {
                    // allready done
                } else {
                    // d != NO because only disctinguish assigns NO -- and it has not been called
                    // for this indes
                    distinguish(leftSi, rightSi);
                }
            }
         }
    }

    /**
     * Distinguish. pre: leftSi > rightSi
     */
    private void distinguish(int leftSi, int rightSi) {
        int leftTi, maxLeftTi, rightTi, maxRightTi;
        int leftEndSi, rightEndSi;
        Range leftRange, rightRange;
        IntArrayList tmp;
        boolean foundUnknown;  // true, if a state with unknown distinct is found

        maxLeftTi = fa.get(leftSi).size();
        if (maxLeftTi == 0) {
            throw new IllegalArgumentException("fa not complete");
        }
        foundUnknown = false;
    leftTransitions:
        for (leftTi = 0; leftTi < maxLeftTi; leftTi++) {
            leftRange = (Range) fa.get(leftSi).getInput(leftTi);
            leftEndSi = fa.get(leftSi).getEnd(leftTi);
            maxRightTi = fa.get(rightSi).size();
            for (rightTi = 0; rightTi < maxRightTi; rightTi++) {
                rightRange = (Range) fa.get(rightSi).getInput(rightTi);
                if (leftRange.touches(rightRange)) {
                    do {
                        rightEndSi = fa.get(rightSi).getEnd(rightTi);
                        tmp = getCheckedDistinctAndAllocate(leftEndSi, rightEndSi);
                        if (tmp == YES) {
                            // (leftEndSi, rightEndSi) are known to differ.
                            setDistinct(leftSi, rightSi);
                            return;
                        } else if (tmp == NO) {
                            // do nothing
                        } else {
                            foundUnknown = true;
                            // distinct for (leftEndSi, rightEndSi) is
                            // not known. Set (leftSi, rightSi) on its list.
                            tmp.add(pair(leftSi, rightSi));
                        }
                        rightTi++;
                        if (rightTi == maxRightTi) {
                            continue leftTransitions;
                        }
                        rightRange = (Range) fa.get(rightSi).getInput(rightTi);
                    } while (leftRange.touches(rightRange));
                    // ranges are sortet; thus, all touching ranges follow without gaps
                    continue leftTransitions;
                }
            }
            throw new IllegalArgumentException("not a cdfa");
        }
        if (!foundUnknown) {
            // all follow upstates are "NO"
            distinct[leftSi][rightSi] = NO;
        }
    }

    /** Mark the pair to be distinct. Recursively marks depending pairs. */
    private void setDistinct(int leftSi, int rightSi) {
        IntArrayList tmp;
        int pair;
        int i, max;

        tmp = distinct[leftSi][rightSi];
        if (tmp != YES) {
            distinct[leftSi][rightSi] = YES;
            if (tmp != UNKNOWN) {
                max = tmp.size();
                for (i = 0; i < max; i++) {
                    pair = tmp.get(i);
                    setDistinct(left(pair), right(pair));
                }
            }
        }
    }

    private IntArrayList getCheckedDistinctAndAllocate(int leftSi, int rightSi) {
        IntArrayList tmp;

        if (leftSi > rightSi) {
            tmp = distinct[leftSi][rightSi];
            if (tmp == UNKNOWN) {
                tmp = new IntArrayList();
                distinct[leftSi][rightSi] = tmp;
            }
            return tmp;
        } else if (leftSi == rightSi) {
            return NO;
        } else {
            tmp = distinct[rightSi][leftSi];
            if (tmp == UNKNOWN) {
                tmp = new IntArrayList();
                distinct[rightSi][leftSi] = tmp;
            }
            return tmp;
        }
    }

    private static int pair(int left, int right) {
        return left << 16 | right;
    }

    private static int left(int pair) {
        return pair >>> 16;
    }

    private static int right(int pair) {
        return pair & 0xffff;
    }
}
