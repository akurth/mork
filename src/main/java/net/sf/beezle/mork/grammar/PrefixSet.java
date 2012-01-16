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

package net.sf.beezle.mork.grammar;

import net.sf.beezle.mork.misc.StringArrayList;

import java.util.*;

public class PrefixSet implements Iterable<Prefix> {
    /** the average lookahead size for k = 1 in Java and Ssass is 17 */
    private static final int DEFAULT_INITIAL_CAPACITY = 32;
    private static final float LOAD_FACTOR = 0.75f;
    private static final long FREE = -1;

    public static PrefixSet one(int symbol) {
        PrefixSet result;

        result = new PrefixSet();
        result.add(Prefix.forSymbol(symbol).data);
        return result;
    }

    public static PrefixSet zero() {
        PrefixSet result;

        result = new PrefixSet();
        result.add(Prefix.EMPTY.data);
        return result;
    }

    //--

    private long[] table;
    private int size;

    private int threshold;

    private int collisions;

    public PrefixSet() {
        this.threshold = (int) (DEFAULT_INITIAL_CAPACITY * LOAD_FACTOR);
        this.table = new long[DEFAULT_INITIAL_CAPACITY];
        Arrays.fill(table, FREE);
    }

    public PrefixSet(PrefixSet orig) {
        this.threshold = orig.threshold;
        this.table = new long[orig.table.length];
        this.size = orig.size;
        System.arraycopy(orig.table, 0, table, 0, table.length);
    }

    public Iterator<Prefix> iterator() {
        return new PrefixIterator();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean add(long prefix) {
        int i;
        long cmp;

        for (i = indexFor(Prefix.hashCode(prefix), table.length); true; i = (i + 1) % table.length) {
            cmp = table[i];
            if (cmp == FREE) {
                table[i] = prefix;
                if (size++ >= threshold) {
                    resize(2 * table.length);
                }
                return false;
            }
            if (cmp == prefix) {
                return true;
            }
            collisions++;
        }
    }

    public void addAll(PrefixSet set) {
        for (Prefix prefix : set) {
            add(prefix.data);
        }
    }

    public boolean equals(Object o) {
        PrefixSet set;

        /* No o == this check: PefixSets *are* shared when shifting, but in this case, the cores always differ */
        if (o instanceof PrefixSet) {
            set = (PrefixSet) o;
            if (set.size != size) {
                return false;
            }
            for (Prefix p : set) {
                if (!lookup(p)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        return size;
    }

    public void toString(StringArrayList symbolTable, StringBuilder result) {
        boolean first;
        List<Prefix> sorted;

        // TODO: expensive
        sorted = new ArrayList<Prefix>();
        Collections.sort(sorted);
        result.append('{');
        first = true;
        for (Prefix entry : sorted) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            entry.toString(symbolTable, result);
        }
        result.append('}');
    }

    public List<int[]> follows(int first) {
        List<int[]> result;
        int[] terminals;

        result = new ArrayList<int[]>();
        for (Prefix prefix : this) {
            terminals = prefix.follows(first);
            if (terminals != null) {
                result.add(terminals);
            }
        }
        return result;
    }

    //--

    private boolean lookup(Prefix prefix) {
        int hash;
        int i;
        long cmp;

        hash = prefix.hashCode();
        for (i = indexFor(hash, table.length); true; i = (i + 1) % table.length) {
            cmp = table[i];
            if (cmp == FREE) {
                return false;
            }
            if (cmp == prefix.data) {
                return true;
            }
        }
    }

    private void resize(int size) {
        long[] oldTable;
        long prefix;

        oldTable = table;
        table = new long[size];
        Arrays.fill(table, FREE);
        for (int i = 0; i < oldTable.length; i++) {
            prefix = oldTable[i];
            if (prefix != FREE) {
                table[indexFor(new Prefix(prefix).hashCode(), size)] = prefix;
            }
        }
        threshold = (int) (size * LOAD_FACTOR);
    }

    //--

    private static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    //--

    private class PrefixIterator implements Iterator<Prefix> {
        private int index;

        public PrefixIterator() {
            if (size > 0) {
                index = 0;
                step();
            } else {
                index = table.length;
            }
        }

        public boolean hasNext() {
            return index < table.length;
        }

        public Prefix next() {
            Prefix result;

            if (index >= table.length) {
                throw new NoSuchElementException();
            }
            result = new Prefix(table[index++]);
            step();
            return result;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void step() {
            for (; index < table.length; index++) {
                if (table[index] != FREE) {
                    break;
                }
            }
        }
    }
}