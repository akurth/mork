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

public class Conflict {
    private final int state;
    private final int symbol;
    private final int actionA;
    private final int actionB;

    public Conflict(int state, int symbol, int actionA, int actionB) {
        this.state = state;
        this.symbol = symbol;
        this.actionA = actionA;
        this.actionB = actionB;
    }

    public String toString(StringArrayList symbolTable) {
        return "state " + state + " on symbol " + symbolTable.getOrIndex(symbol) + ": " + ParserTable.actionToString(actionA)
                    + " vs " + ParserTable.actionToString(actionB) + "\n";
    }
}
