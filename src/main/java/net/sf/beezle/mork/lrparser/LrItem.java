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

package net.sf.beezle.mork.lrparser;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.List;
import java.util.Map;

/** LR(1) item. */
public class LrItem extends Item implements Comparable<LrItem> {
    public final int production;
    /** grammar.getProdLength() for end */
    public final int dot;
    public final IntBitSet lookahead;

    public LrItem(int production, int dot, IntBitSet lookahead) {
        this.production = production;
        this.dot = dot;
        this.lookahead = lookahead;
    }

    /** @return symbol of -1 if nothing can be shifted */
    public int getShift(Grammar grammar) {
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
            return new LrItem(production, dot + 1, lookahead);
        }
    }

    public void expanded(Grammar grammar, IntBitSet nullable, Map<Integer, IntBitSet> firsts, List<LrItem> result) {
        int symbol;
        int alt, maxAlt;
        LrItem item;

        symbol = getShift(grammar);
        if (symbol == -1) {
            // nothing to shift
        } else {
            maxAlt = grammar.getAlternativeCount(symbol);
            for (alt = 0; alt < maxAlt; alt++) {
                item = new LrItem(grammar.getAlternative(symbol, alt), 0, first(grammar, nullable, firsts, production, dot + 1, lookahead));
                if (!result.contains(item)) {
                    result.add(item);
                }
            }
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
        return production * dot;
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
        return (production == cmp.production) && (dot == cmp.dot);
    }

    public int compareTo(LrItem obj) {
        LrItem item;

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
                // TODO: lookahead
                return 0;
            }
        }
    }

    public String toString(LrPDA env, StringArrayList symbolTable) {
        StringBuilder result;
        int ofs, len;

        result = new StringBuilder();
        result.append(symbolTable.getOrIndex(env.grammar.getLeft(production)));
        result.append("\t::=");
        len = env.grammar.getLength(production);
        for (ofs = 0; ofs < len; ofs++) {
            result.append(' ');
            if (ofs == dot) {
                result.append(". ");
            }
            result.append(symbolTable.getOrIndex(env.grammar.getRight(production, ofs)));
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
