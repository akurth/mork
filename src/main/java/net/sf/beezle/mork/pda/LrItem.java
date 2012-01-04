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
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.List;
import java.util.Map;

/** LR(1) item. */
public class LrItem implements Comparable<LrItem> {
    /** production with dot */
    private final int core;

    public final IntBitSet lookahead;

    public LrItem(int production, int dot, IntBitSet lookahead) {
        this((production << 8) | dot, lookahead);
    }

    public LrItem(int core, IntBitSet lookahead) {
        this.core = core;
        this.lookahead = lookahead;
    }

    public int getProduction() {
        return core >> 8;
    }

    /** grammar.getProdLength() for end */
    public int getDot() {
        return (byte) core;
    }

    /** @return symbol of -1 if nothing can be shifted */
    public int getShift(Grammar grammar) {
        int production;
        int dot;

        production = getProduction();
        dot = getDot();
        if (dot < grammar.getLength(production)) {
            return grammar.getRight(production, dot);
        } else {
            return -1;
        }
    }

    public LrItem createShifted(Grammar grammar) {
        int symbol;

        symbol = getShift(grammar);
        if (symbol == -1) {
            return null;
        } else {
            return new LrItem(core + 1, lookahead);
        }
    }

    public void expanded(Grammar grammar, IntBitSet nullable, Map<Integer, IntBitSet> firsts, List<LrItem> result) {
        int symbol;
        int alt, maxAlt;
        LrItem item;
        int production;
        int dot;

        production = getProduction();
        dot = getDot();
        if (dot < grammar.getLength(production)) {
            symbol = grammar.getRight(production, dot);
            maxAlt = grammar.getAlternativeCount(symbol);
            for (alt = 0; alt < maxAlt; alt++) {
                item = new LrItem(grammar.getAlternative(symbol, alt), 0, first(grammar, nullable, firsts, production, dot + 1, lookahead));
                if (!result.contains(item)) {
                    result.add(item);
                }
            }
        } else {
            // nothing to shift
        }
    }

    private static IntBitSet first(Grammar grammar, IntBitSet nullable, Map<Integer, IntBitSet> firsts,
            int production, int dot, IntBitSet lookahead) {
        int symbol;
        IntBitSet result;

        result = new IntBitSet();
        for (int ofs = dot; ofs < grammar.getLength(production); ofs++) {
            symbol = grammar.getRight(production, ofs);
            result.addAll(firsts.get(symbol));
            if (!nullable.contains(symbol)) {
                return result;
            }
        }
        result.addAll(lookahead);
        return result;
    }

    //--

    @Override
    public int hashCode() {
        return core;
    }

    @Override
    public boolean equals(Object obj) {
        LrItem cmp;

        if (obj instanceof LrItem) {
            cmp = (LrItem) obj;
            return sameCore(cmp) && lookahead.equals(cmp.lookahead);
        } else {
            return false;
        }
    }

    public boolean sameCore(LrItem cmp) {
        return core == cmp.core;
    }

    public int compareTo(LrItem obj) {
        LrItem item;

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

    public String toString(Grammar grammar) {
        int production;
        int dot;
        StringArrayList symbolTable;
        StringBuilder result;
        int ofs, len;

        production = getProduction();
        dot = getDot();
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
        result.append(" \t");
        result.append(lookahead.toString(symbolTable.toList()));
        result.append('\n');
        return result.toString();
    }
}
