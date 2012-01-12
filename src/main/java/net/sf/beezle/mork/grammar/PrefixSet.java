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
    private static final char[] EMPTY = new char[0];
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final float LOAD_FACTOR = 0.75f;

    public static PrefixSet one(int k, int symbol) {
        PrefixSet result;

        result = new PrefixSet(k);
        result.addSymbol(symbol);
        return result;
    }

    public static PrefixSet zero(int k) {
        PrefixSet result;

        result = new PrefixSet(k);
        result.add(EMPTY);
        return result;
    }

    //--

    public final int k;
    private Prefix[] table;
    private int size;

    private int threshold;

    public PrefixSet(int k) {
        this.threshold = (int) (DEFAULT_INITIAL_CAPACITY * LOAD_FACTOR);
        this.table = new Prefix[DEFAULT_INITIAL_CAPACITY];
        this.k = k;
    }

    public PrefixSet(PrefixSet orig) {
        this.threshold = orig.threshold;
        this.table = new Prefix[orig.table.length];
        this.k = orig.k;
        addAll(orig);
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

    public boolean addSymbol(int symbol) {
        char[] data;

        data = new char[] { (char) symbol };
        if (data[0] != symbol) {
            throw new IllegalArgumentException("" + symbol);
        }
        return add(data);
    }

    public boolean add(char[] data) {
        int i;
        Prefix cmp;

        i = indexFor(Prefix.hashCode(data), table.length);
        for (cmp = table[i]; cmp != null; cmp = cmp.next) {
            if (cmp.eq(data)) {
                return true;
            }
        }
        table[i] = new Prefix(data, table[i]);
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
        return false;
    }

    public void addAll(PrefixSet set) {
        for (Prefix prefix : set) {
            add(prefix.data);
        }
    }

    public boolean equals(Object o) {
        PrefixSet set;

        if (o instanceof PrefixSet) {
            set = (PrefixSet) o;
            if (set.size != size) {
                return false;
            }
            for (Prefix p : set) {
                if (lookup(p) == null) {
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

    private Prefix lookup(Prefix prefix) {
        int hash;

        hash = prefix.hashCode();
        for (Prefix cmp = table[indexFor(hash, table.length)]; cmp != null; cmp = cmp.next) {
            if (prefix.eq(cmp)) {
                return cmp;
            }
        }
        return null;
    }

    private void resize(int newCapacity) {
        Prefix[] oldTable;
        int oldCapacity;
        Prefix[] newTable;

        oldTable = table;
        oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        newTable = new Prefix[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)(newCapacity * LOAD_FACTOR);
    }

    private void transfer(Prefix[] newTable) {
        Prefix[] src;
        int newCapacity;
        Prefix prefix;

        src = table;
        newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            prefix = src[j];
            if (prefix != null) {
                do {
                    Prefix next = prefix.next;
                    int i = indexFor(prefix.hashCode(), newCapacity);
                    prefix.next = newTable[i];
                    newTable[i] = prefix;
                    prefix = next;
                } while (prefix != null);
            }
        }
    }

    //--

    // internal utilities

    /**
     * Returns index for hash code h.
     */
    private static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    //--

    private class PrefixIterator implements Iterator<Prefix> {
        private Prefix next;
        private int index;

        public PrefixIterator() {
            if (size > 0) { // advance to first entry
                Prefix[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public Prefix next() {
            Prefix prefix;
            Prefix[] t;

            prefix = next;
            if (prefix == null) {
                throw new NoSuchElementException();
            }
            next = prefix.next;
            if (next == null) {
                t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
            return prefix;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
