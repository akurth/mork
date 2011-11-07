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

package de.mlhartme.mork.semantics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Stupid data container for CopyBuffer */
public class Merger {
    public final List<State> source;

    /** attribute resulting from the merger */
    public final Attribute dest;

    // to have unique names
    private static int count = 0;

    public Merger(int destSymbol, Type destType) {
        source = new ArrayList<State>();
        dest = new Attribute(destSymbol, "merged" + count, destType);
        count++;
    }

    public static Merger forSymbol(List<Merger> mergers, int symbol) {
        int i;
        int max;
        Merger merger;

        max = mergers.size();
        for (i = 0; i < max; i++) {
            merger = (Merger) mergers.get(i);
            if (merger.dest.symbol == symbol) {
                return merger;
            }
        }
        return null;
    }

    public static Attribute map(Map<Attribute, Merger> mapping, Attribute attr) {
        Merger merger;

        merger = mapping.get(attr);
        if (merger != null) {
            return merger.dest;
        } else {
            return attr;
        }
    }
}
