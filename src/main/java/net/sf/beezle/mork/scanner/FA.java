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

import net.sf.beezle.sushi.util.IntBitSet;

/**
 * Finite automaton, deteministic or non-deterministic. Has at
 * least 1 state. Always has a start state. Maintains a set of end
 * states.
 */

public class FA {
    /* 0 would cause endless loops in ensureCapacity. */
    private static final int INITIAL_STATES = 128;

    /** Main state of this class. */
    private State[] states;

    /** First unused index in states. */
    private int used;

    /** Start state. -1 if no start state is set. */
    private int start;

    /** End states */
    private IntBitSet ends;

    //-- construction

    public FA() {
        states = new State[INITIAL_STATES];
        start = -1;
        used = 0;
        ends = new IntBitSet();
    }

    public FA(FA orig) {
        int i;

        states = new State[orig.states.length];
        used = orig.used;
        for (i = 0; i < used; i++) {
            states[i] = new State(orig.states[i], 0);
        }
        start = orig.start;
        ends = new IntBitSet(orig.ends);
    }

    //--

    private void ensureCapacity(int idx) {
        int size;
        State[] grown;

        if (idx >= states.length) {
            size = states.length;
            while (idx >= size) {
                size *= 2;
                if (size < 0) {
                    // Overflow. Prevend endless loops
                    size = idx + 1;
                    break;
                }
            }

            grown = new State[size];
            System.arraycopy(states, 0, grown, 0, used);
            states = grown;
        }
    }

    private void checkState(int state) {
        if ((state > used) || (state < 0)) {
            throw new IllegalArgumentException();
        }
    }

    //-- state primitives

    /**
     * Inserts a new states to the automaton. New states are
     * allocated at the end.
     * @param label label for the state created.
     */
    public int add(Object label) {
        ensureCapacity(used);
        states[used] = new State(label);
        return used++;
    }

    public State get(int idx) {
        return states[idx];
    }

    public int size() {
        return used;
    }

    public int find(Object label) {
        int i;

        if (label == null) {
            throw new IllegalArgumentException();
        }
        for (i = 0; i < used; i++) {
            if (label.equals(states[i].getLabel())) {
                return i;
            }
        }
        return -1;
    }

    public void setEndLabels(Object label) {
        int si;

        for (si = ends.first(); si != -1; si = ends.next(si)) {
            states[si].setLabel(label);
        }
    }

    //-- special states

    /**
     * @return -1 if no start state has been set.
     */
    public int getStart() {
        return start;
    }

    /**
     * @param state -1 to unset the start state
     */
    public void setStart(int state) {
        start = state;
    }

    public void setEnd(int state) {
        checkState(state);

        ends.add(state);
    }

    public void resetEnd(int state) {
        checkState(state);

        ends.remove(state);
    }

    public boolean isEnd(int state) {
        checkState(state);

        return ends.contains(state);
    }
    public int getFirstEnd() {
        return ends.first();
    }

    public int getNextEnd(int state) {
        return ends.next(state);
    }

    //-- combinatation of FA

    public void sequence(FA seq) {
        int idx;
        int relocation;

        relocation = append(seq);

        if (seq.start != -1) {
            for (idx = ends.first(); idx != -1; idx = ends.next(idx)) {
                states[idx].add(relocation + seq.start, null);
            }
        }

        ends.clear();
        for (idx = seq.ends.first(); idx != -1; idx = seq.ends.next(idx)) {
            ends.add(idx + relocation);
        }
    }

    public void alternate(FA alt) {
        int relocation;
        int newStart, newEnd;
        int idx;

        relocation = append(alt);

        newStart = add(null);
        if (start != -1) {
            states[newStart].add(start, null);
        }
        if (alt.start != -1) {
            states[newStart].add(alt.start + relocation, null);
        }
        start = newStart;

        newEnd = add(null);
        for (idx = ends.first(); idx != -1; idx = ends.next(idx)) {
            states[idx].add(newEnd, null);
        }
        for (idx = alt.ends.first(); idx != -1; idx = alt.ends.next(idx)) {
            states[relocation + idx].add(newEnd, null);
        }
        ends.clear();
        ends.add(newEnd);
    }

    public void star() {
        loop(true);
    }
    public void plus() {
        loop(false);
    }

    private void loop(boolean optional) {
        int newStart, newEnd;
        int idx;

        newStart = add(null);
        newEnd = add(null);
        if (optional) {
            states[newStart].add(newEnd, null);
        }
        if (start != -1) {
            states[newStart].add(start, null);
        }
        for (idx = ends.first(); idx != -1; idx = ends.next(idx)) {
            if (start != -1) {
                states[idx].add(start, null);
            }
            states[idx].add(newEnd, null);
        }
        start = newStart;
        ends.clear();
        ends.add(newEnd);
    }

    /**
     * Negates normal states and end states. If the automaton is deterministic,
     * the accepted language is negated.
     */
    public void not() {
        IntBitSet tmp;

        tmp = new IntBitSet();
        tmp.addRange(0, used - 1);
        tmp.removeAll(ends);
        ends = tmp;
    }

    /**
     * Adds a copy of all states and transitions from the automaton
     * specified. No transition is added between old and new states.
     * States and transitions are relocated with the offsets specified.
     */
    private int append(FA add) {
        int relocation;
        int idx;

        relocation = used;
        ensureCapacity(used + add.used);
        for (idx = 0; idx < add.used; idx++) {
            states[relocation + idx] =
                new State(add.states[idx], relocation);
        }
        used += add.used;
        return relocation;
    }

    //-- analysis

    /**
     * Its too expensive to compute a single epsilong closure.
     */
    public IntBitSet[] epsilonClosures() {
        int si;
        int nextSize;
        IntBitSet[] result;
        IntBitSet tmp;
        int[] size;
        boolean repeat;

        result = new IntBitSet[used];
        size = new int[used];
        for (si = 0; si < used; si++) {
            tmp = new IntBitSet();
            states[si].epsilonClosure(tmp);
            result[si] = tmp;
            size[si] = tmp.size();
        }

        do {
            repeat = false;
            for (si = 0; si < used; si++) {
                result[si].addAllSets(result);
                nextSize = result[si].size();
                if (nextSize > size[si]) {
                    size[si] = nextSize;
                    repeat = true;
                }
            }
        } while (repeat);

        return result;
    }

    //--

    @Override
    public String toString() {
        StringBuilder result;
        int idx;

        result = new StringBuilder();
        result.append("start = " + start + " end = " + ends.toString() + "\n");
        for (idx = 0; idx < used; idx++) {
            result.append("  " + idx + " ");
            result.append(states[idx].toString());
            result.append("\n");
        }
        result.append("fa end\n");

        return result.toString();
    }
}
