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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PrefixSet extends AbstractSet<Prefix> {
    public static PrefixSet one(int k, int symbol) {
        PrefixSet result;

        result = new PrefixSet(k);
        result.add(new Prefix(symbol));
        return result;
    }

    //--

    public final int k;
    private final HashMap<Prefix,Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    public PrefixSet(int k) {
        this.map = new HashMap<Prefix, Object>();
        this.k = k;
    }

    public PrefixSet(PrefixSet orig) {
        map = new HashMap<Prefix, Object>(Math.max((int) (orig.size()/.75f) + 1, 16));
        addAll(orig);
        this.k = orig.k;
    }

    public void toString(StringArrayList symbolTable, StringBuilder result) {
        boolean first;
        List<Prefix> sorted;

        // TODO: expensive
        sorted = new ArrayList<Prefix>(this);
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

    public Iterator<Prefix> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean add(Prefix e) {
        return map.put(e, PRESENT)==null;
    }

    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }

    public void clear() {
        map.clear();
    }
}
