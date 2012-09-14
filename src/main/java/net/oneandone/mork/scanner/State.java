/**
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
package net.oneandone.mork.scanner;

import net.oneandone.mork.regexpr.Range;
import net.oneandone.sushi.util.IntBitSet;

/**
 * State in a NFA. A state is a label and a sequence of transitions.
 */
public class State {
    /* 0 would cause endless loops in ensureCapacity. */
    private static final int INITIAL_TRANSITIONS = 16;

    private int used;

    /** Associates user data with this state; may be null. */
    private Object label;

    /** States where transitions end. */
    private int[] ends;

    /**
     * Associates user data with transitions; may be null.
     * The user data is the input to consume when following the
     * transition, null is interpreted as epsilon transition.
     */
    private Range[] inputs;

    //--

    public State(Object labelInit) {
        label = labelInit;
        used = 0;
        ends = new int[INITIAL_TRANSITIONS];
        inputs = new Range[INITIAL_TRANSITIONS];
    }

    public State(State orig, int relocation) {
        int i;

        label = orig.label;
        used = orig.used;
        ends = new int[orig.ends.length];
        for (i = 0; i < used; i++) {
            ends[i] = orig.ends[i] + relocation;
        }
        inputs = new Range[orig.inputs.length];
        System.arraycopy(orig.inputs, 0, inputs, 0, used);
    }

    //--

    private void ensureCapacity(int idx) {
        int size;
        int[] grownEnds;
        Range[] grownInputs;

        if (idx >= inputs.length) {
            size = inputs.length; // same as ends.length
            while (idx >= size) {
                size *= 2;
                if (size < 0) {
                    // overflow. prevent endless loops
                    size = idx + 1;
                    break;
                }
            }

            grownEnds = new int[size];
            System.arraycopy(ends, 0, grownEnds, 0, used);
            ends = grownEnds;

            grownInputs = new Range[size];
            System.arraycopy(inputs, 0, grownInputs, 0, used);
            inputs = grownInputs;
        }
    }

    private void checkTransition(int transition) {
        if ((transition >= used) || (transition < 0)) {
            throw new IllegalArgumentException();
        }
    }

    //--

    public Object getLabel() {
        return label;
    }

    public void setLabel(Object labelInit) {
        label = labelInit;
    }

    /** @return a sorted list of transition indexes. */
    public int[] sortRanges() {
        IntBitSet tis;
        int ti;
        Range range;
        int minTi;
        int minRangeFirst;
        int[] result;
        int ofs;
        int cmp;

        tis = new IntBitSet();
        tis.addRange(0, size() - 1); // may be empty;
        result = new int[size()];
        for (ofs = 0; ofs < result.length; ofs++) {
            ti = tis.first();
            minTi = ti;
            minRangeFirst = getInput(ti).getFirst();
            ti = tis.next(ti);
            while (ti != -1) {
                range = getInput(ti);
                cmp = range.getFirst();
                if (cmp < minRangeFirst) {
                    minRangeFirst = cmp;
                    minTi = ti;
                }
                ti = tis.next(ti);
            }

            tis.remove(minTi);
            result[ofs] = minTi;
        }
        return result;
    }


    //--

    public int size() {
        return used;
    }

    public int add(int state, Range input) {
        ensureCapacity(used);
        ends[used] = state;
        inputs[used] = input;
        return used++;
    }

    public int getEnd(int transition) {
        checkTransition(transition);
        return ends[transition];
    }

    public Range getInput(int transition) {
        checkTransition(transition);
        return inputs[transition];
    }

    public void epsilonClosure(IntBitSet states) {
        int transition;

        for (transition = 0; transition < used; transition++) {
            if (inputs[transition] == null) {
                states.add(ends[transition]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result;
        int transition;
        int i;
        int[] intLabel;

        result = new StringBuilder();
        if (label == null) {
            result.append("<null>");
        } else {
            result.append(label.toString());
        }
        result.append("\t");
        for (transition = 0; transition < used; transition++) {
            result.append('\t');
            if (inputs[transition] == null) {
                result.append("<eps>");
            } else {
                result.append(inputs[transition].toString());
            }
            result.append("->" + ends[transition]);
        }

        return result.toString();
    }
}
