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
package net.oneandone.mork.parser;

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.pda.State;

public class Conflict {
    private final String type;
    private final int stateId;
    private final State state;
    private final int symbol;
    private final int[] actions;

    public Conflict(String type, int stateId, State state, int symbol, int ... actions) {
        this.type = type;
        this.stateId = stateId;
        this.state = state;
        this.symbol = symbol;
        this.actions = actions;
    }

    public String toString(Grammar grammar) {
        StringBuilder builder;
        boolean first;
        
        builder = new StringBuilder();
        builder.append(type + " conflict in state " + stateId + " on symbol ");
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
