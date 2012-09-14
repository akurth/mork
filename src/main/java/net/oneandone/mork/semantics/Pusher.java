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
package net.oneandone.mork.semantics;

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Attribute grammar, supports >=0 synthesized and inherited attributes.
 * Uses lazy evaluation to calculate attributes. There are no pre-calculated
 * computation sequence and no checks for cyclic dependencies are done at
 * run-time.
 */
public class Pusher {
    /** states to be pushed */
    private final List<State> states;

    private final IntBitSet border;
    private final Grammar grammar;

    public static AgBuffer run(boolean down, Attribute seed, IntBitSet border, Grammar grm) {
        Pusher pusher;

        pusher = new Pusher(seed, border, grm);
        if (down) {
            pusher.pushDown();
        } else {
            pusher.pushUp();
        }
        // drop the seed
        pusher.states.remove(0);
        return new AgBuffer(pusher.states);
    }

    private Pusher(Attribute seed, IntBitSet border, Grammar grammar) {
        this.states = new ArrayList<State>();
        this.states.add(new State(seed));
        this.border = border;
        this.grammar = grammar;
    }

    private void pushUp() {
        int i;
        int user;
        int max;
        State state;
        Attribute attr;

        // states.size() grows
        for (i = 0; i < states.size(); i++) {
            state = states.get(i);
            attr = state.getAttribute();
            if (i == 0 || !border.contains(attr.symbol)) {
                max = grammar.getUserCount(attr.symbol);
                for (user = 0; user < max; user++) {
                    pushUp(user, i);
                }
            }
        }
    }

    private void pushUp(int user, int attrIdx) {
        State child;
        Attribute childAttr;
        int prod;
        int symbol;
        State parent;
        int max;
        int idx;
        int ofs;

        child = states.get(attrIdx);
        childAttr = child.getAttribute();
        prod = grammar.getUser(childAttr.symbol, user);
        symbol = grammar.getLeft(prod);
        max = grammar.getUserOfsCount(childAttr.symbol, user);
        for (idx = 0; idx < max; idx++) {
            parent = findState(1, symbol);
            if (parent == null) {
                parent = new State(true, new Attribute(symbol, "transport"), grammar);
                states.add(parent);
            }
            ofs = grammar.getUserOfs(childAttr.symbol, user, idx);
            parent.addUpTransport(prod, ofs, childAttr);
        }
    }

    private void pushDown() {
        Attribute attr;
        int i;
        int max;
        int alt;

        // states.size() grows
        for (i = 0; i < states.size(); i++) {
            attr = states.get(i).getAttribute();
            if (i == 0 || !border.contains(attr.symbol)) {
                max = grammar.getAlternativeCount(attr.symbol);
                for (alt = 0; alt < max; alt++) {
                    pushDown(alt, i);
                }
            }
        }
    }

    private void pushDown(int alt, int attrIdx) {
        State child;
        int prod;
        int symbol;
        State parent;
        Attribute parentAttr;
        int max;
        int ofs;

        parent = states.get(attrIdx);
        parentAttr = parent.getAttribute();
        prod = grammar.getAlternative(parentAttr.symbol, alt);
        max = grammar.getLength(prod);
        for (ofs = 0; ofs < max; ofs++) {
            symbol = grammar.getRight(prod, ofs);
            child = findState(1, symbol);
            if (child == null) {
                child = new State(false, new Attribute(symbol, "transport"), grammar);
                states.add(child);
            }
            child.addDownTransport(prod, ofs, parentAttr);
        }
    }

    private State findState(int ofs, int symbol) {
        int i;
        int max;
        State state;

        max = states.size();
        for (i = ofs; i < max; i++) {
            state = states.get(i);
            if (state.getAttribute().symbol == symbol) {
                return state;
            }
        }
        return null;
    }
}
