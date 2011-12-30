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

package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.StringArrayList;

import java.util.Collection;

/** LR(0) item. Immutable. */
public class LalrItem extends BaseItem implements Comparable<LalrItem> {
    public final int production;
    public final int dot;

    public LalrItem(int production, int dot) {
        this.production = production;
        this.dot = dot;
    }

    //--

    @Override
    public int hashCode() {
        return production * dot;
    }

    @Override
    public boolean equals(Object obj) {
        LalrItem item;

        if (obj instanceof LalrItem) {
            item = (LalrItem) obj;
            return (production == item.production) && (dot == item.dot);
        } else {
            return false;
        }
    }

    public int compareTo(LalrItem obj) {
        LalrItem item;

        item = obj;
        if (production < item.production) {
            return -1;
        } else if (production > item.production) {
            return 1;
        } else {
            if (dot < item.dot) {
                return -1;
            } else if (dot > item.dot) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    //--

    /** @return symbol of -1 if nothing can be shifted */
    public int getShift(Grammar grammar) {
        if (dot < grammar.getLength(production)) {
            return grammar.getRight(production, dot);
        } else {
            return -1;
        }
    }

    public LalrItem createShifted() {
        return new LalrItem(production, dot + 1);
    }

    public void addClosure(Grammar grammar, Collection<LalrItem> result) {
        int symbol;

        symbol = getShift(grammar);
        if (symbol != -1) {
            addClosure(grammar, symbol, result);
        } else {
            // reduce item, nothing to do
        }
    }

    public static void addClosure(Grammar grammar, int symbol, Collection<LalrItem> result) {
        int alt, maxAlt;

        maxAlt = grammar.getAlternativeCount(symbol);
        for (alt = 0; alt < maxAlt; alt++) {
            result.add(new LalrItem(grammar.getAlternative(symbol, alt), 0));
        }
    }

    //--

    public String toString(Grammar grammar) {
        StringArrayList symbolTable;
        StringBuilder result;
        int ofs, len;

        symbolTable = grammar.getSymbolTable();
        result = new StringBuilder();
        result.append(symbolTable.getOrIndex(grammar.getLeft(production)));
        result.append("\t::=");
        len = grammar.getLength(production);
        for (ofs = 0; ofs < len; ofs++) {
            result.append(' ');
            if (ofs == dot) {
                result.append(". ");
            }
            result.append(symbolTable.getOrIndex(grammar.getRight(production, ofs)));
        }
        if (ofs == dot) {
            result.append(" .");
        }
        result.append(" \n");  // no semicolon
        return result.toString();
    }
}
