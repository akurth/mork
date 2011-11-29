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

package net.sf.beezle.mork.parser;

import net.sf.beezle.mork.misc.StringArrayList;

import java.util.ArrayList;
import java.util.List;

public class Conflicts extends Exception {
    private final List<int[]> conflicts;

    public Conflicts() {
        conflicts = new ArrayList<int[]>();
    }

    public boolean isEmpty() {
        return conflicts.isEmpty();
    }

    public void add(int state, int sym, int actionA, int actionB) {
        conflicts.add(new int[] { state, sym, actionA, actionB });
    }

    public String toString(StringArrayList symbolTable) {
        StringBuilder result;

        result = new StringBuilder();
        for (int[] c : conflicts) {
            result.append("state " + c[0] + " on symbol " + symbolTable.getOrIndex(c[1]) + "\n");
        }
        return result.toString();
    }
}
