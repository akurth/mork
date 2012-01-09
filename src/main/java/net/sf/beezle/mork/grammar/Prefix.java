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

/**
 * List of int elements. Similar to java.lang.List or
 * java.util.ArrayList, but it stores primitive int values.
 * Generic collections for primitive types whould remove the
 * need for IntArrayList. I implemented only those methods
 * that I acually need.
 */
public class Prefix implements Comparable<Prefix> {
    // TODO
    public static Prefix create(int element) {
        Prefix result;

        result = new Prefix();
        result.add(element);
        return result;
    }

    /** Storage for elements. */
    private int[] data;

    /** Number of data elements actually used. */
    private int size;

    /** Creates a new empty List, initial size is 32. */
    public Prefix() {
        this(32);
    }

    private Prefix(int initialSize) {
        data = new int[initialSize];
        size = 0;
    }

    private Prefix(Prefix orig) {
        data = new int[orig.data.length];
        size = orig.size;
        System.arraycopy(orig.data, 0, data, 0, size);
    }

    public int get(int idx) {
        return data[idx];
    }

    public void ensureCapacity(int min) {
        int[] tmp;
        int old;
        int capacity;

        old = data.length;
        if (min > old) {
            tmp = data;
            capacity = (old * 5) / 3 + 1;
            if (capacity < min) {
                capacity = min;
            }
            data = new int[capacity];
            System.arraycopy(tmp, 0, data, 0, size);
        }
    }

    public void add(int idx, int ele) {
        ensureCapacity(size + 1);
        System.arraycopy(data, idx, data, idx + 1, size - idx);
        data[idx] = ele;
        size++;
    }

    public void add(int ele) {
        ensureCapacity(size + 1);
        data[size++] = ele;
    }

    public Prefix concat(Prefix right, int k) {
        Prefix result;

        if (size() > k) {
            throw new IllegalArgumentException();
        }
        result = new Prefix(this);
        for (int i = 0; i < right.size(); i++) {
            if (result.size() >= k) {
                break;
            }
            result.add(right.get(i));
        }
        return result;
    }

    public int indexOf(int ele) {
        int i;

        for (i = 0; i < size; i++) {
            if (data[i] == ele) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(int ele) {
        return indexOf(ele) != -1;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder buffer;
        int i, max;

        max = size();
        buffer = new StringBuilder();
        for (i = 0; i < max; i++) {
            buffer.append(' ');
            buffer.append(get(i));
        }
        return buffer.toString();
    }

    @Override
    public int hashCode() {
        return size();
    }

    @Override
    public boolean equals(Object obj) {
        Prefix operand;
        int i;

        if (obj instanceof Prefix) {
            operand = (Prefix) obj;
            if (size == operand.size) {
                for (i = 0; i < size; i++) {
                    if (data[i] != operand.data[i]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Prefix right) {
        if (size == right.size()) {
            for (int i = 0; i < size; i++) {
                if (get(i) < right.get(i)) {
                    return -1;
                } else if (get(i) > right.get(i)) {
                    return 1;
                }
            }
            return 0;
        } else {
            return size - right.size();
        }
    }
}
