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

package net.sf.beezle.mork.semantics;

import net.sf.beezle.mork.mapping.Transport;
import net.sf.beezle.sushi.util.IntArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Attribute grammar, supports >=0 synthesized and inherited attributes.
 * Uses lazy evaluation to calculate attributes. There are no pre-calculated
 * computation sequence and no checks for cyclic dependencies are done at
 * run-time.
 */
public class AgBuffer implements Compare {
    /**
     * List of States
     */
    private final List<State> states;

    private Attribute start;

    public AgBuffer(Attribute start) {
        this.start = start;
        this.states = new ArrayList<State>();
    }

    public AgBuffer(List<State> states) {
        this.states = states;
    }

    public Attribute getStart() {
        return start;
    }

    public void setStart(Attribute start) {
        this.start = start;
    }

    public void append(AgBuffer right) {
        for (State rightState : right.states) {
            add(rightState);
        }
    }

    public void add(State right) {
        State left;

        left = lookup(right.getAttribute());
        if (left == null) {
            left = new State(right);
            states.add(left);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void createSemanticsBuffer(Ag sems, Transport transport) {
        for (State state : states) {
            state.createSemanticsBuffer(sems, transport);
        }
    }

    /**
     * Can be used for both down and up transport.
     * TODO: creates new CopyLines only, attributes are shared between all
     * restrictions. This works as long as the resulting SemanticsBuffer is "cloned with creating
     * new attributes" ...
     */
    public AgBuffer createReduced(Attribute start) {
        List<Attribute> attrs;
        int i;
        Attribute attr;
        State state;
        AgBuffer reduced;

        reduced = new AgBuffer(start);
        attrs = new ArrayList<Attribute>();
        attrs.add(start);
        // size usually grows
        for (i = 0; i < attrs.size(); i++) {
            attr = attrs.get(i);
            state = lookup(attr);
            if (state != null) {
                reduced.add(state);
                state.addArgAttrs(attrs);
            }
        }
        return reduced;
    }

    /**
     * Is down Optional.
     * pre:   this is kind of reduced (e.g. results from createReduced)
     */
    public boolean isDownOptional() {
        for (State state : states) {
            if (state.isDownOptional()) {
                return true;
            }
        }
        return false;
    }

    //---------------------------------------------------------------
    // remove duplicate transport attributes

    /**
     * TODO: get rid of this method ...
     *
     * @return all attributes being result or argument of at least one attribution.
     */
    public List<Attribute> getTransportAttributes() {
        List<Attribute> result;

        result = new ArrayList<Attribute>();
        for (State state : states) {
            result.add(state.getAttribute());
        }
        return result;
    }

    public State lookup(Attribute attr) {
        for (State state : states) {
            if (attr.equals(state.getAttribute())) {
                return state;
            }
        }
        return null;
    }

    //----------------------------------------------------------

    /**
     * Merge all attributes with >0 attributions buffers. Return the merged attribute of the
     * specified symbol.
     */
    public Attribute merge(List<AgBuffer> copyBuffers, int symbol, Type mergedType) {
        Map<Attribute, Merger> mapping;    // map old attribute to merger objects. null: don't merge.
        List<Merger> mergers;
        Merger merger;
        int i;
        int max;

        mapping = new HashMap<Attribute, Merger>();
        mergers = new ArrayList<Merger>();
        max = copyBuffers.size();
        for (i = 0; i < max; i++) {
            copyBuffers.get(i).createMergers(mergers, mapping, mergedType);
        }
        runMergers(mergers, mapping);
        merger = Merger.forSymbol(mergers, symbol);
        if (merger == null) {
            throw new IllegalStateException();
        }
        return merger.dest;
    }

    private void createMergers(List<Merger> mergers, Map<Attribute, Merger> mapping, Type mergedType) {
        Attribute attr;
        Merger merger;
        State state;
        Iterator<State> iter;

        iter = states.iterator();
        while (iter.hasNext()) {
            state = iter.next();
            attr = state.getAttribute();
            merger = Merger.forSymbol(mergers, attr.symbol);
            if (merger == null) {
                merger = new Merger(attr.symbol, mergedType);
                mergers.add(merger);
            }
            merger.source.add(state);
            mapping.put(attr, merger);
        }
    }

    private void runMergers(List<Merger> mergers, Map<Attribute, Merger> mapping) {
        int i;
        int max;
        Merger merger;
        State state;

        max = mergers.size();
        for (i = 0; i < max; i++) {
            merger = mergers.get(i);
            state = State.merge(mapping, merger.source);
            states.add(state);
        }
    }


    /**
     * @return LT, GT, or NE
     */
    public int compare(AgBuffer rightSemantics) {
        List<Attribute> lefts; // list of left attributes compared
        List<Attribute> rights;  // list of attributes compared
        int i;  // index of the current attributes to be compared
        int cmp;
        int result;
        Attribute left;
        Attribute right;

        left = start;
        right = rightSemantics.start;

        if (left.symbol != right.symbol) {
            throw new IllegalArgumentException();
        }

        lefts = new ArrayList<Attribute>();
        lefts.add(left);
        rights = new ArrayList<Attribute>();
        rights.add(right);

        result = EQ;
        for (i = 0; i < rights.size(); i++) {
            if (lefts.size() != rights.size()) {
                throw new IllegalStateException();
            }
            cmp = localCompare(lefts.get(i), rights.get(i), rightSemantics, lefts, rights);
            if (cmp != EQ) {
                if (cmp == NE) {
                    return NE;
                } else {
                    if (result == EQ) {
                        result = cmp;
                    } else {
                        // the second state which is not EQ  --> NE, even if cmp == result
                        return NE;
                    }
                }
            } else {
                // do nothing, no news
            }
        }
        if (result == EQ) {
            // this method does not return NE
            return NE;
        }
        return result;
    }

    /**
     * Adds only states to left and right that are reached by eq states
     * Never returns ALT.
     */
    private int localCompare(
        Attribute left, Attribute right, AgBuffer rightSemantics, List<Attribute> nextLefts, List<Attribute> nextRights)
    {
        State leftState;
        State rightState;

        if (left.symbol != right.symbol) {
            throw new IllegalStateException();
        }
        leftState = lookup(left);
        rightState = rightSemantics.lookup(right);
        if (leftState == null && rightState == null) {
            return EQ;
        }
        if (leftState == null || rightState == null) {
            throw new IllegalStateException("different number of productions");
        }
        return leftState.compare(rightState, nextLefts, nextRights);
    }

    //------------------

    public String toRawString() {
        return toString(true);
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean raw) {
        StringBuilder buf;

        buf = new StringBuilder();
        for (State state : states) {
            buf.append(state.toString(raw));
        }
        return buf.toString();
    }

    /**
     * Clones this buffer but replaces all transport attributes with new attributes
     * of the specified type.
     *
     * @param type for new attributes
     * @return cloned seed
     */
    public Attribute cloneAttributes(AgBuffer orig, Type type, Attribute seed) {
        Map<Attribute, Attribute> map; // old attributes to new attributes
        Attribute attr;
        State newState;

        map = new HashMap<Attribute, Attribute>();
        for (State state : orig.states) {
            attr = state.getAttribute();
            map.put(attr, new Attribute(attr.symbol, null, type));
        }
        for (State state : orig.states) {
            newState = state.cloneAttributeTransport(map, orig);
            states.add(newState);
        }
        attr = map.get(seed);
        if (attr == null) {
            throw new IllegalArgumentException();
        }
        return attr;
    }

    //----------------------------------------------------------------------------

    // TODO: drop card, use calcOccurrence instead ...
    public int calcCard(Attribute root) {
        Occurrence occ;

        occ = calcOccurrence(root);
        return occ.card();
    }

    // TODO: use calcOccurrences instead
    public Occurrence calcOccurrence(Attribute start) {
        Occurrence occ;

        occ = calcOccurrence(new ArrayList(), start);
        if (occ == null) {
            throw new IllegalStateException();
        }
        return occ;
    }

    public Occurrence calcOccurrence(List stack, Attribute start) {
        State state;
        Occurrence occ;

        state = lookup(start);
        if (state == null) {
            return Occurrence.ONE;
        }
        stack.add(start);
        occ = state.calcOccurrence(this, stack);
        stack.remove(stack.size() - 1);
        return occ;
    }

    //-----------------------------------------------------------------------------------

    public int getWidth(Attribute attr) {
        State state;

        state = lookup(attr);
        if (state == null) {
            return 1;
        } else {
            return state.maxOcc;
        }
    }

    // TODO: nice and simple -- but it fails for recursion
    public void calcOccurrences() {
        State state;
        Iterator iter;
        boolean changes;

        iter = states.iterator();
        while (iter.hasNext()) {
            state = (State) iter.next();
            state.minOcc = 1;
            state.maxOcc = 1;
        }
        do {
            // TODO: iterating in reverse order might be faster
            changes = false;
            iter = states.iterator();
            while (iter.hasNext()) {
                state = (State) iter.next();
                if (state.recalcOccurrence(this)) {
                    changes = true;
                }
            }
        } while (changes);
    }

    public Attribute createSequence(Attribute start, int seq, AgBuffer result) {
        Attribute attr;

        attr = createSequence(start, seq, new ArrayList<State>(), new ArrayList<State>(), result);
        return attr;
    }

    private Attribute createSequence(
        Attribute attr, int seq, List<State> origStack, List<State> clonedStack, AgBuffer result)
    {
        State orig;
        State clone;
        List nextAttrs;
        IntArrayList nextOfss;
        IntArrayList nextSeqs;
        int alt;
        int altCount;
        Attribute tmp;
        int nextSeq;
        Attribute nextAttr;
        int idx;

        orig = lookup(attr);
        if (orig != null) {
            idx = origStack.indexOf(orig);
            if (idx != -1) {
                clone = (State) clonedStack.get(idx);
            } else {
                clone = State.cloneEmpty(orig);
                nextAttrs = new ArrayList();
                nextOfss = new IntArrayList();
                nextSeqs = new IntArrayList();
                orig.getSequence(seq, nextAttrs, nextOfss, nextSeqs, this);
                altCount = nextAttrs.size();
                origStack.add(orig);
                clonedStack.add(clone);
                for (alt = 0; alt < altCount; alt++) {
                    nextAttr = (Attribute) nextAttrs.get(alt);
                    if (nextAttr != null) {
                        nextSeq = nextSeqs.get(alt);
                        tmp = createSequence(nextAttr, nextSeq, origStack, clonedStack, result);
                        clone.addBlind(alt, tmp, nextOfss.get(alt));
                    }
                }
                origStack.remove(origStack.size() - 1);
                clonedStack.remove(clonedStack.size() - 1);
                result.states.add(clone);
            }
            return clone.getAttribute();
        } else {
            return attr;
        }
    }
}
