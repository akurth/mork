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

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.pda.State;

public class Conflict {
    private final String type;
    private final State state;
    private final int symbol;
    private final int[] actions;

    public Conflict(String type, State state, int symbol, int ... actions) {
        this.type = type;
        this.state = state;
        this.symbol = symbol;
        this.actions = actions;
    }

    public String toString(Grammar grammar) {
        StringBuilder builder;
        boolean first;
        
        builder = new StringBuilder();
        builder.append(type + " conflict in state " + state.id + " on symbol ");
        builder.append(grammar.getSymbolTable().getOrIndex(symbol));
        builder.append(": ");
        first = true;
        for (int action : actions) {
            if (first) {
                first = false;
            } else {
                builder.append(" vs ");
            }
            builder.append(ParserTable.actionToString(action, grammar));
        }
        builder.append("\n").append(state.toShortString(grammar));
        return builder.toString();
    }
}
