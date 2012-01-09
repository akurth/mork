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
import net.sf.beezle.sushi.util.IntArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class PrefixSet extends HashSet<Prefix> {
    public static PrefixSet single(int k, int symbol) {
        PrefixSet result;

        result = new PrefixSet(k);
        result.add(one(symbol));
        return result;
    }

    public static Prefix one(int symbol) {
        Prefix result;

        result = new Prefix();
        result.add(symbol);
        return result;
    }

    //--

    public final int k;

    public PrefixSet(int k) {
        this.k = k;
    }

    public void toString(StringArrayList symbolTable, StringBuilder result) {
        boolean first;
        List<Prefix> sorted;

        // TODO: expensive
        sorted = new ArrayList<Prefix>(this);
        Collections.sort(sorted, new Comparator<Prefix>() {
            @Override
            public int compare(Prefix left, Prefix right) {
                int max;

                max = left.size();
                if (max == right.size()) {
                    for (int i = 0; i < max; i++) {
                        if (left.get(i) < right.get(i)) {
                            return -1;
                        } else if (left.get(i) > right.get(i)) {
                            return 1;
                        }
                    }
                    return 0;
                } else {
                    return max - right.size();
                }
            }
        });
        result.append('{');
        first = true;
        for (Prefix entry : sorted) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            for (int i = 0, max = entry.size(); i < max; i++) {
                if (i > 0) {
                    result.append(' ');
                }
                result.append(symbolTable.getOrIndex(entry.get(i)));
            }
        }
        result.append('}');
    }

    public List<int[]> follows(int first) {
        List<int[]> result;
        int[] terminals;

        result = new ArrayList<int[]>();
        for (Prefix entry : this) {
            if (entry.size() > 0) {
                if (entry.get(0) == first) {
                    terminals = new int[entry.size() - 1];
                    for (int i = 0; i < terminals.length; i++) {
                        terminals[i] = entry.get(i + 1);
                    }
                    result.add(terminals);
                }
            }
        }
        return result;
    }
}
