/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.pda;

import net.oneandone.mork.grammar.Concat;
import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.grammar.PrefixSet;
import net.oneandone.mork.misc.StringArrayList;

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
