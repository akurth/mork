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

package de.mlhartme.mork.parser;

import de.mlhartme.mork.misc.StringArrayList;

import java.util.Collection;

/** Immutable. */

public class Item implements Comparable<Item> {
    public final int production;
    public final int dot;

    public Item(int productionInit, int dotInit) {
        production = productionInit;
        dot = dotInit;
    }

    //---------------------------------------------------------------

    @Override
    public int hashCode() {
        return production * dot;
    }

    @Override
    public boolean equals(Object obj) {
        Item item;

        if (obj instanceof Item) {
            item = (Item) obj;
            return (production == item.production) && (dot == item.dot);
        } else {
            return false;
        }
    }

    public int compareTo(Item obj) {
        Item item;

        item = (Item) obj;
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

    //-----------------------------------------------------------------

    public int getShift(PDA env) {
        if (dot < env.grm.getLength(production)) {
            return env.grm.getRight(production, dot);
        } else {
            return -1;
        }
    }

    public Item createShifted() {
        return new Item(production, dot + 1);
    }

    public void addExpansion(PDA env, Collection<Item> result) {
        int symbol;

        symbol = getShift(env);
        if (symbol != -1) {
            addExpansion(env, symbol, result);
        } else {
            // reduce item, nothing to do
        }
    }

    public static void addExpansion(PDA env, int symbol, Collection<Item> result) {
        int alt, maxAlt;

        maxAlt = env.grm.getAlternativeCount(symbol);
        for (alt = 0; alt < maxAlt; alt++) {
            result.add(new Item(env.grm.getAlternative(symbol, alt), 0));
        }
    }

    //-------------------------------------------------------------------

    public String toString(PDA env, StringArrayList symbolTable) {
        StringBuilder result;
        int ofs, len;

        result = new StringBuilder();
        result.append(symbolTable.getOrIndex(env.grm.getLeft(production)));
        result.append("\t::=");
        len = env.grm.getLength(production);
        for (ofs = 0; ofs < len; ofs++) {
            result.append(' ');
            if (ofs == dot) {
                result.append(". ");
            }
            result.append(symbolTable.getOrIndex(
                              env.grm.getRight(production, ofs)));
        }
        if (ofs == dot) {
            result.append(" .");
        }
        result.append(" \n");  // no semicolon
        return result.toString();
    }
}
