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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PrefixSet implements Iterable<Prefix> {
    public static PrefixSet one(int k, int symbol) {
        PrefixSet result;

        result = new PrefixSet(k);
        result.add(new Prefix(symbol));
        return result;
    }

    //--

    public final int k;
    private final HMap map;

    public PrefixSet(int k) {
        this.map = new HMap();
        this.k = k;
    }

    public PrefixSet(PrefixSet orig) {
        map = new HMap(Math.max((int) (orig.size()/.75f) + 1, 16));
        this.k = orig.k;
        addAll(orig);
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

    public Iterator<Prefix> iterator() {
        return map.newKeyIterator();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Prefix o) {
        return map.containsKey(o);
    }

    public boolean add(Prefix e) {
        return map.put(e);
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
        int h = 0;
        Iterator<Prefix> i = iterator();
        while (i.hasNext()) {
            Prefix obj = i.next();
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }
}
