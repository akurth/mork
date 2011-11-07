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

package de.mlhartme.mork.misc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: dump
 * 
 * List of Strings. Similar to java.util.List or java.util.ArrayList,
 * but elements are Strings. Generic collections for Java would remove
 * the need for StringArrayList. I chose to name the class StringArrayList
 * instead of StringArrayList because it is shorter.
 * I have implemented only those methods from ArrayList that I actually
 * need.
 */

public class StringArrayList implements Serializable {
    /** The amount by which the capacity of data is increased when necessary. */
    private static final int GROW = 256;

    /** Storage for elements. May contain null, even at indexes < size. */
    private String[] data;

    /** Number of elements actually used in data. */
    private int size;

    public StringArrayList(String[] data) {
        this(data.length, data);
    }

    public StringArrayList(int size, String[] data) {
        this.size = size;
        this.data = data;
    }

    //--------------------------------------------------------------

    /**
     * Creates a new empty List
     */
    public StringArrayList() {
        data = new String[GROW];
        size = 0;
    }

    /**
     * Copy constructor.
     * @param  orig  List that supplies the initial elements for
     *               the new List.
     */
    public StringArrayList(StringArrayList orig) {
        data = new String[orig.data.length];
        size = orig.size;
        System.arraycopy(orig.data, 0, data, 0, size);
    }

    //----------------------------------------------------------------
    // operations

    /**
     * Adds a new element to the end of the List.
     * @param  str  new element
     */
    public void add(String str) {
        String[] tmp;

        if (size == data.length) {
            tmp = new String[data.length + GROW];
            System.arraycopy(data, 0, tmp, 0, data.length);
            data = tmp;
        }
        data[size] = str;
        size++;
    }

    /**
     * Adds a whole List of elements.
     * @param  vec   List of elements to add
     */
    public void addAll(StringArrayList vec) {
        int i;
        int max;

        max = vec.size();
        for (i = 0; i < max; i++) {
            add(vec.get(i));
        }
    }

    /**
     * Lookup an element.
     * @param   str  element to find
     * @return  index of the first element found; -1 if nothing was found
     */
    public int indexOf(String str) {
        int i;

        for (i = 0; i < size; i++) {
            if (data[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Retrieves an element.
     * @param  idx  selects the element to get
     * @return the selected element; may be null
     */
    public String get(int idx) {
        return data[idx];
    }

    public String getOrIndex(int idx) {
        String result;

        if ((idx >= 0) && (idx < data.length)) {
            result = data[idx];
            if (result != null) {
                return result;
            }
        }
        return "<" + idx + ">";
    }

    /**
     * Replaces an element
     * @param  idx  selects the element to be set
     * @param  str  New value for the selected element; may be null
     */
    public void set(int idx, String str) {
        data[idx] = str;
    }

    public void remove(int idx) {
        int i;

        size--;
        for (i = idx; i < size; i++) {
            data[i] = data[i + 1];
        }
    }

    /**
     * Returns the number of elements.
     * @return  number of elements
     */
    public int size() {
        return size;
    }


    //---------------------------------------------------------------------


    // TODO
    public List<String> toList() {
        List<String> result;
        
        result = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            result.add(data[i]);
        }
        return result;
    }

    /**
     * Returns the string representation.
     * @return  string representation
     */
    @Override
    public String toString() {
        StringBuilder buf;
        int i;

        buf = new StringBuilder();
        buf.append("StringArrayList {\n");
        for (i = 0; i < size; i++) {
            if (data[i] != null) {
                buf.append(" " + i + "\t= " + data[i] + "\n");
            }
        }
        buf.append("}\n");
        return buf.toString();
    }
}
