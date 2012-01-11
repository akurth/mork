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

import java.util.Arrays;

/** Immutable */
public class Prefix implements Comparable<Prefix> {
    public static final Prefix EMPTY = new Prefix(new char[] {});

    private final char[] data;

    public Prefix(int first) {
        this(new char[] { (char) first });
        if (((char) first) != first) {
            throw new IllegalArgumentException("" + first);
        }
    }

    /** Private because the caller has to ensure the array is passed to nobody else (and modified) */
    private Prefix(char[] data) {
        this.data = data;
    }

    public int first() {
        return data[0];
    }

    public Prefix concat(Prefix right, int k) {
        char[] next;

        if (data.length == 0) {
            return right;
        }
        next = new char[Math.min(k, size() + right.size())];
        System.arraycopy(data, 0, next, 0, size());
        System.arraycopy(right.data, 0, next, size(), next.length - size());
        return new Prefix(next);
    }

    public int size() {
        return data.length;
    }

    @Override
    public String toString() {
        StringBuilder buffer;
        int i, max;

        max = size();
        buffer = new StringBuilder();
        for (i = 0; i < max; i++) {
            buffer.append(' ');
            buffer.append(data[i]);
        }
        return buffer.toString();
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
        return data.length == 0 ? 0 : data[0] + data[data.length - 1] << 8;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Prefix) {
            return eq((Prefix) obj);
        }
        return false;
    }

    public boolean eq(Prefix operand) {
        return Arrays.equals(data, operand.data);
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
