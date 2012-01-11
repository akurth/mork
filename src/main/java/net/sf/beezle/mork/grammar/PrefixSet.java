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
    /**
     * The default initial capacity - MUST be a power of two.
     */
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float LOAD_FACTOR = 0.75f;

    public static PrefixSet one(int k, int symbol) {
        PrefixSet result;

        result = new PrefixSet(k);
        result.add(new Prefix(symbol));
        return result;
    }

    //--

    public final int k;
    private Entry[] table;
    private int size;

    /**
     * The next size value at which to resize (capacity * load factor).
     * @serial
     */
    private int threshold;

    public PrefixSet(int k) {
        this.threshold = (int)(DEFAULT_INITIAL_CAPACITY * LOAD_FACTOR);
        this.table = new Entry[DEFAULT_INITIAL_CAPACITY];
        this.k = k;
    }

    public PrefixSet(PrefixSet orig) {
        int initialCapacity = Math.max((int) (orig.size()/.75f) + 1, 16);

        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            throw new IllegalArgumentException();
        }
        this.k = orig.k;

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity)
            capacity <<= 1;

        threshold = (int)(capacity * LOAD_FACTOR);
        table = new Entry[capacity];

        addAll(orig);
    }

    public Iterator<Prefix> iterator() {
        return new PrefixIterator();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Prefix prefix) {
        return lookup(prefix) != null;
    }

    public boolean add(Prefix prefix) {
        int hash = hash(prefix.hashCode());
        int i = indexFor(hash, table.length);
        for (Entry e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.prefix) == prefix || prefix.equals(k))) {
                return true;
            }
        }
        Entry e = table[i];
        table[i] = new Entry(hash, prefix, e);
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
        return false;
    }

    public void addAll(PrefixSet set) {
        for (Prefix e : set) {
            add(e);
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof PrefixSet)) {
            return false;
        }

        PrefixSet c = (PrefixSet) o;
        if (c.size() != size()) {
            return false;
        }
        for (Prefix e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return size();
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

    private Entry lookup(Prefix prefix) {
        int hash = hash(prefix.hashCode());
        for (Entry e = table[indexFor(hash, table.length)];
             e != null;
             e = e.next) {
            Object k;
            if (e.hash == hash && ((k = e.prefix) == prefix || prefix.equals(k))) {
                return e;
            }
        }
        return null;
    }

    private void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)(newCapacity * LOAD_FACTOR);
    }

    private void transfer(Entry[] newTable) {
        Entry[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry next = e.next;
                    int i = indexFor(e.hash, newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    //--

    // internal utilities

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits. Note: Null keys always map to hash 0, thus index 0.
     */
    private static int hash(int h) {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns index for hash code h.
     */
    private static int indexFor(int h, int length) {
        return h & (length-1);
    }

    //--

    private static class Entry {
        /** never null */
        public final Prefix prefix;
        public Entry next;
        public final int hash;

        Entry(int h, Prefix p, Entry n) {
            next = n;
            prefix = p;
            hash = h;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry e = (Entry) o;
            return prefix.equals(e.prefix);
        }

        public final int hashCode() {
            return prefix.hashCode();
        }

        public final String toString() {
            return prefix.toString();
        }
    }

    private class PrefixIterator implements Iterator<Prefix> {
        Entry next;        // next entry to return
        int index;              // current slot
        Entry current;     // current entry

        public PrefixIterator() {
            if (size > 0) { // advance to first entry
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public Prefix next() {
            Entry e = next;
            if (e == null) {
                throw new NoSuchElementException();
            }

            if ((next = e.next) == null) {
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null)
                    ;
            }
            current = e;
            return e.prefix;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
