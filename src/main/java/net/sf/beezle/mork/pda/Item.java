/**
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

import net.sf.beezle.mork.grammar.Concat;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.grammar.PrefixSet;
import net.sf.beezle.mork.misc.StringArrayList;

import java.util.List;
import java.util.Map;

/** LR(k) item. */
public class Item implements Comparable<Item> {
    public static Item create(Grammar grammar, int production,  PrefixSet lookahead) {
        return new Item((production << 8) | grammar.getLength(production), lookahead);
    }

    /** production with remaining. remaining = prodLength - dot */
    public final int core;

    /** do NOT inline this object into Item because the lookahead is shared when shifting */
    public final PrefixSet lookahead;

    private Item(int core, PrefixSet lookahead) {
        this.core = core;
        this.lookahead = lookahead;
    }

    public int getProduction() {
        return core >> 8;
    }

    /** grammar.getProdLength() for end */
    public int getRemaining() {
        return (byte) core;
    }

    /** @return symbol of -1 if nothing can be shifted */
    public int getShift(Grammar grammar) {
        int production;
        int remaining;

        remaining = getRemaining();
        if (remaining == 0) {
            return -1;
        } else {
            production = getProduction();
            return grammar.getRight(production, grammar.getLength(production) - remaining);
        }
    }

    public boolean isReduce() {
        return getRemaining() == 0;
    }

    public Item createShifted() {
        if (isReduce()) {
            return null;
        } else {
            return new Item(core - 1, lookahead);
        }
    }

    public void expanded(Grammar grammar, Map<Integer, PrefixSet> firsts, List<Item> result, int k) {
        int symbol;
        int alt, maxAlt;
        Item item;
        int production;
        int remaining;
        int dot;
        PrefixSet first;

        remaining = getRemaining();
        if (remaining > 0) {
            production = getProduction();
            dot = grammar.getLength(production) - remaining;
            symbol = grammar.getRight(production, dot);
            maxAlt = grammar.getAlternativeCount(symbol);
            for (alt = 0; alt < maxAlt; alt++) {
                first = first(grammar, firsts, production, dot + 1, lookahead, k);
                item = Item.create(grammar, grammar.getAlternative(symbol, alt), first);
                if (!result.contains(item)) {
                    result.add(item);
                }
            }
        } else {
            // nothing to shift
        }
    }

    private static PrefixSet first(Grammar grammar, Map<Integer, PrefixSet> firsts, int production, int dot, PrefixSet lookahead, int k) {
        int len;
        int symbol;
        Concat concat;

        len = grammar.getLength(production);
        if (len == dot) {
            return new PrefixSet(lookahead);
        }
        concat = new Concat(k);
        for (int ofs = dot; ofs < len; ofs++) {
            symbol = grammar.getRight(production, ofs);
            if (concat.with(firsts.get(symbol))) {
                return concat.result();
            }
        }
        concat.with(lookahead);
        return concat.result();
    }

    //--

    @Override
    public int hashCode() {
        return core;
    }

    @Override
    public boolean equals(Object obj) {
        Item cmp;

        if (obj instanceof Item) {
            cmp = (Item) obj;
            return core == cmp.core && lookahead.equals(cmp.lookahead);
        } else {
            return false;
        }
    }

    public int compareTo(Item obj) {
        Item item;

        item = obj;
        if (core < item.core) {
            return -1;
        } else if (core > item.core) {
            return 1;
        } else {
            // TODO: lookahead
            return 0;
        }
    }

    public String toString(Grammar grammar, boolean suppressLookahead) {
        int production;
        int dot;
        StringArrayList symbolTable;
        StringBuilder result;
        int ofs, len;

        production = getProduction();
        dot = grammar.getLength(production) - getRemaining();
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
        if (ofs == dot && !suppressLookahead) {
            result.append(" . \t");
            lookahead.toString(symbolTable, result);
        }
        result.append('\n');
        return result.toString();
    }
}
