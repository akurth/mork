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

import java.util.ArrayList;
import java.util.List;

import net.sf.beezle.sushi.util.IntArrayList;

/**
 * Helper class to instantiate symbols.
 */

public class PreSymbol {
    /** productions for this symbol */
    private IntArrayList alternatives;

    /** productions using this symbol. */
    private IntArrayList users;

    /** ofsets in the using productions. List of IntArrayLists. */
    private List<IntArrayList> userOfs;

    public PreSymbol() {
        alternatives = new IntArrayList();
        users = new IntArrayList();
        userOfs = new ArrayList<IntArrayList>();
    }

    public void addAlternative(int prod) {
        alternatives.add(prod);
    }

    public void addUser(int prod, int ofs) {
        int i;
        IntArrayList ofsArray;

        i = users.indexOf(prod);
        if (i == -1) {
            i = users.size();
            users.add(prod);
            userOfs.add(new IntArrayList());
        }

        ofsArray = (IntArrayList) userOfs.get(i);
        ofsArray.add(ofs);
    }

    public Symbol createSymbol() {
        int[][] ofss;
        int i;
        IntArrayList ofsArray;

        ofss = new int[users.size()][];
        for (i = 0; i < ofss.length; i++) {
            ofsArray = (IntArrayList) userOfs.get(i);
            ofss[i] = ofsArray.toArray();
        }

        return new Symbol(alternatives.toArray(), users.toArray(), ofss);
    }
}
