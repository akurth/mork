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

public class PrefixSet {
    /** Right part of prime computed with PrefixSetTest. */
    public static final int[] SIZES = {
        // the average lookahead size for k = 1 in Java and Ssass is 17
        31,
        73,
        199,
        421,
        883,
        1873,
        3673,
        7333,
        14869,
        29389,
        57559,
        116533,
        234961,
        489871,
        999961
    };

    private int nextSize(int oldSize) {
        for (int i = 0; i < SIZES.length - 1; i++) {
            if (oldSize == SIZES[i]) {
                return SIZES[i + 1];
            }
        }
        throw new IllegalStateException();
    }

    public static final long FREE = -1;

    public static PrefixSet one(int ... symbols) {
        PrefixSet result;

        result = new PrefixSet();
        result.addUnpacked(symbols);
        return result;
    }

    //--

    private long[] table;
    private int size;
    private int collisions;

    public PrefixSet() {
        this.table = new long[SIZES[0]];
        Arrays.fill(table, FREE);
    }

    public PrefixSet(PrefixSet orig) {
        this.table = new long[orig.table.length];
        this.size = orig.size;
        System.arraycopy(orig.table, 0, table, 0, table.length);
    }

    public Prefix iterator() {
        return new Prefix(table, size);
    }

    /** average comparisons for successfull search */
    public double hashQuality() {
        return ((double) collisions + size) / size;
    }

    public double load() {
        return (double) size / table.length;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean addUnpacked(int ... symbols) {
        return add(Prefix.pack(symbols));
    }

    public boolean add(long prefix) {
        long cmp;
        long[] old;

        for (int hash = Prefix.hashFirst(prefix, table.length); true; hash = Prefix.hashNext(prefix, hash, table.length)) {
            cmp = table[hash];
            if (cmp == FREE) {
                table[hash] = prefix;
                if (size++ >= table.length * 3 / 4) {
                    old = table;
                    size = 0;
                    collisions = 0;
                    table = new long[nextSize(old.length)];
                    Arrays.fill(table, FREE);
                    for (long p : old) {
                        if (p != FREE) {
                            add(p);
                        }
                    }
                }
                return true;
            }
            if (cmp == prefix) {
                return false;
            }
            collisions++;
        }
    }

    public void addAll(PrefixSet set) {
        Prefix prefix;

        prefix = set.iterator();
        while (prefix.step()) {
            add(prefix.data);
        }
    }

    public boolean equals(Object o) {
        PrefixSet set;
        Prefix prefix;

        /* No o == this check: PefixSets *are* shared when shifting, but in this case, the cores always differ */
        if (o instanceof PrefixSet) {
            set = (PrefixSet) o;
            if (set.size != size) {
                return false;
            }
            prefix = set.iterator();
            while (prefix.step()) {
                if (notContains(prefix.data)) {
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

    public String toString(StringArrayList symbolTable) {
        StringBuilder result;

        result = new StringBuilder();
        toString(symbolTable, result);
        return result.toString();
    }

    public void toString(StringArrayList symbolTable, StringBuilder result) {
        boolean first;
        Prefix prefix;

        result.append('{');
        first = true;
        prefix = iterator();
        while (prefix.step()) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            prefix.toString(symbolTable, result);
        }
        result.append(" }");
    }

    public List<int[]> follows(int first) {
        List<int[]> result;
        int[] terminals;
        Prefix prefix;


        result = new ArrayList<int[]>();
        prefix = iterator();
        while (prefix.step()) {
            terminals = prefix.follows(first);
            if (terminals != null) {
                result.add(terminals);
            }
        }
        return result;
    }

    //--

    private boolean notContains(long prefix) {
        long cmp;

        for (int hash = Prefix.hashFirst(prefix, table.length); true; hash = Prefix.hashNext(prefix, hash, table.length)) {
            cmp = table[hash];
            if (cmp == FREE) {
                return true;
            }
            if (cmp == prefix) {
                return false;
            }
        }
    }
}