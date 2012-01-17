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

/** Element in a prefix set, and an iterator. Immutable, heavily shared between PrefixSets. */
public class Prefix {
    public static final int BASE = 1024;

    public static long pack(int ... symbols) {
        long data;

        data = 0;
        for (int symbol : symbols) {
            data = data * BASE + pack(symbol);
        }
        return data;
    }

    public static long pack(int symbol) {
        if (symbol >= BASE - 1) {
            throw new IllegalArgumentException("" + symbol);
        }
        return symbol + 1;
    }

    public static int[] unpack(long data) {
        int[] result;

        result = new int[size(data)];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = (int) (data % BASE) - 1;
            data /= BASE;
        }
        return result;
    }

    //--

    /** behind current prefix - first index to check for next element */
    private int index;
    private final long[] table;

    /** access only for PrefixSet */
    long data;

    //--

    /** Constructor for PrefixSet only */
    Prefix(long[] table, int size) {
        this.table = table;
        this.index = 0; // we don't have empty tables
    }

    //-- iterator

    public boolean step() {
        for (; index < table.length; index++) {
            if (table[index] != PrefixSet.FREE) {
                data = table[index++];
                return true;
            }
        }
        return false;
    }

    //-- prefix methods

    public int first() {
        long remaining;
        long next;

        remaining = data;
        while (true) {
            next = remaining / BASE;
            if (next == 0) {
                return (int) remaining - 1;
            }
            remaining = next;
        }
    }

    public static long concat(long leftPrefix, long rightPrefix, int k) {
        int leftSize;
        int rightSize;
        long newData;
        int newSize;
        int count;

        if (leftPrefix == 0) {
            return rightPrefix;
        }
        if (rightPrefix == 0) {
            return leftPrefix;
        }
        leftSize = size(leftPrefix);
        if (leftSize == k) {
            return leftPrefix;
        }
        rightSize = Prefix.size(rightPrefix);
        newData = leftPrefix;
        newSize = Math.min(k, leftSize + rightSize);
        count = newSize - leftSize;
        for (int i = count; i > 0; i--) {
            newData *= BASE;
        }
        long tmp = rightPrefix;
        for (int i = rightSize - count; i > 0; i--) {
            tmp /= BASE;
        }
        return newData + tmp;
    }

    public int size() {
        return size(data);
    }

    public static int size(long prefix) {
        int size;

        for (size = 0; prefix != 0; prefix /= BASE) {
            size++;
        }
        return size;
    }

    public int[] follows(int first) {
        int[] terminals;
        int[] symbols;

        symbols = unpack(data);
        if (symbols.length > 0) {
            if (symbols[0] == first) {
                terminals = new int[symbols.length - 1];
                for (int i = 0; i < terminals.length; i++) {
                    terminals[i] = symbols[(i + 1)];
                }
                return terminals;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder;

        builder = new StringBuilder();
        for (int symbol : unpack(data)) {
            builder.append(' ');
            builder.append(symbol);
        }
        return builder.toString();
    }

    public void toString(StringArrayList symbolTable, StringBuilder result) {
        boolean first;

        first = true;
        for (int symbol : unpack(data)) {
            if (first) {
                first = false;
            } else {
                result.append(' ');
            }
            result.append(' ');
            result.append(symbolTable.getOrIndex(symbol));
        }
    }

    public static int hashFirst(long prefix, int length) {
        return (int) (prefix % length);
    }

    public static int hashNext(long prefix, int previous, int length) {
        return (previous + 1 + (int) (prefix % (length - 2))) % length;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Prefix) {
            return data == ((Prefix) obj).data;
        }
        return false;
    }
}
