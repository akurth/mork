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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.beezle.sushi.util.IntBitSet;

import de.mlhartme.mork.grammar.Grammar;

public class Reduce {
    public final int production;
    public final IntBitSet lookahead;

    public final Set<Shift> lookback;

    public Reduce(int productionInit) {
        production = productionInit;
        lookahead = new IntBitSet();

        lookback = new HashSet<Shift>();
    }

    public void calcLookahead() {
        Iterator<Shift> pos;
        Shift sh;

        pos = lookback.iterator();
        while (pos.hasNext()) {
            sh = pos.next();
            sh.addFollow(lookahead);
        }
    }

    //----------------------------------------------------------

    public String toString(Grammar grammar) {
        StringBuilder buffer;

        buffer = new StringBuilder();
        buffer.append("reduce ");
        grammar.prodToString(buffer, production);
        buffer.append(" on ");
        buffer.append(lookahead.toString(grammar.getSymbolTable().toList()));
        buffer.append('\n');

        return buffer.toString();
    }
    
    @Override
    public int hashCode() {
        return production;
    }
    
    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
