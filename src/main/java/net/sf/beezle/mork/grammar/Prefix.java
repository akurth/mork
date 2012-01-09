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
public class Prefix {
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

    /**
     * Copy constructor.
     * @param  orig  List that supplies the initial elements for
     *               the new List.
     */
    public Prefix(Prefix orig) {
        data = new int[orig.data.length];
        size = orig.size;
        System.arraycopy(orig.data, 0, data, 0, size);
    }

    //-----------------------------------------------------------------

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

    /**
     * Gets an element from the List.
     * @param   idx  index of the element asked for
     * @return  selected element
     */
    public int get(int idx) {
        return data[idx];
    }

    /**
     * Replaces an element in the List.
     * @param  ele  new element
     * @param  idx  index of the element to be replaced
     */
    public void set(int idx, int ele) {
        data[idx] = ele;
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

    /**
     * Adds an element to the List. All following elements
     * are moved up by one index.
     * @param  idx  where to insert the new element
     * @param  ele  new element
     */
    public void add(int idx, int ele) {
        ensureCapacity(size + 1);
        System.arraycopy(data, idx, data, idx + 1, size - idx);
        data[idx] = ele;
        size++;
    }

    /**
     * Adds an element to the end of the List.
     * @param  ele  new element
     */
    public void add(int ele) {
        ensureCapacity(size + 1);
        data[size++] = ele;
    }

    public void addAll(Prefix op) {
        ensureCapacity(size + op.size);
        System.arraycopy(op.data, 0, data, size, op.size);
        size += op.size;
    }

    /**
     * Searches an element.
     * @param   ele  element to look for
     * @return  index of the first element found; -1 if nothing was found
     */
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

    /**
     * Returns the number of elements in the List.
     * @return number of elements
     */
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
}
