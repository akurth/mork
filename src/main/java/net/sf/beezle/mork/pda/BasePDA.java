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
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BasePDA<T extends BaseState> {
    /** start symbol */
    public final int start;
    public final Grammar grammar;
    public final List<T> states;

    protected BaseState end;

    public BasePDA(Grammar grammar, int start, List<T> states) {
        this.grammar = grammar;
        this.start = start;
        this.states = states;
    }

    public int size() {
        return states.size();
    }

    public void print(PrintStream dest) {
        int i, max;

        max = states.size();
        for (i = 0; i < max; i++) {
            dest.println(states.get(i).toString(this, grammar));
        }
    }
}
