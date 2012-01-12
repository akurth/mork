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

public class Prefix implements Comparable<Prefix> {
    final char[] data;

    /** for PrefixSet only */
    Prefix next;

    /* for PrefixSet only */
    Prefix(char[] data, Prefix next) {
        this.data = data;
        this.next = next;
    }

    public int first() {
        return data[0];
    }

    public char[] concat(Prefix right, int k) {
        char[] next;

        if (data.length == 0) {
            return right.data;
        }
        next = new char[Math.min(k, size() + right.size())];
        System.arraycopy(data, 0, next, 0, size());
        System.arraycopy(right.data, 0, next, size(), next.length - size());
        return next;
    }

    public int size() {
        return data.length;
    }

    @Override
    public String toString() {
        StringBuilder builder;
        int i, max;

        max = size();
        builder = new StringBuilder();
        for (i = 0; i < max; i++) {
            builder.append(' ');
            builder.append(data[i]);
        }
        return builder.toString();
    }

    public void toString(StringArrayList symbolTable, StringBuilder result) {
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(symbolTable.getOrIndex(data[i]));
        }
    }

    public int[] follows(int first) {
        int[] terminals;

        if (size() > 0) {
            if (data[0] == first) {
                terminals = new int[size() - 1];
                for (int i = 0; i < terminals.length; i++) {
                    terminals[i] = data[(i + 1)];
                }
                return terminals;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return hashCode(data);
    }

    public static int hashCode(char[] data) {
        int h;

        h = data.length == 0 ? 0 : data[0] + (data[data.length - 1] << 8);
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Prefix) {
            return eq((Prefix) obj);
        }
        return false;
    }

    public boolean eq(Prefix operand) {
        return eq(operand.data);
    }

    public boolean eq(char[] right) {
        int length;
        char[] left;

        left = data;
        length = left.length;
        if (length != right.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (left[i] != right[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(Prefix right) {
        int diff;

        diff = data.length - right.data.length;
        if (diff != 0) {
            return diff;
        }
        for (int i = 0; i < data.length; i++) {
            diff = data[i] - right.data[i];
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }
}
