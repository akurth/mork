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

import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.ParserTable;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public abstract class BasePDA<T extends BaseState> implements Iterable<T> {
    protected final Grammar grammar;
    private final List<T> states;
    protected T start;

    public BasePDA(Grammar grammar, List<T> states) {
        this.grammar = grammar;
        this.states = states;
        this.start = null;
    }

    public Iterator<T> iterator() {
        return states.iterator();
    }

    public void add(T state) {
        if (start == null) {
            start = state;
        }
        states.add(state);
    }
    
    public T addIfNew(T state) {
        int idx;
        
        idx = states.indexOf(state);
        if (idx == -1) {
            add(state);
            return state;
        }
        return states.get(idx);
    }

    public int size() {
        return states.size();
    }

    /**
     * Pseudo-symbol, indicates end-of-file (or an empty word if lookahead is seen as a set of words of length <= 1)
     */
    public int getEofSymbol() {
        return grammar.getSymbolCount();
    }

    public void print(PrintStream dest) {
        for (BaseState state : this) {
            dest.println(state.toString(grammar));
        }
    }

    public ParserTable createTable(int lastSymbol, ConflictHandler handler) throws GenericException {
        // the initial syntaxnode created by the start action is ignoed!
        ParserTable result;
        int eof;
        BaseState end;

        eof = getEofSymbol();
        result = new ParserTable(0, size(), lastSymbol + 1 /* +1 for EOF */, eof, grammar, null);
        for (BaseState state : this) {
            state.addActions(result, handler);
        }
        end = start.lookupShift(grammar.getStart()).end;
        result.addAccept(end.id, eof);
        return result;
    }
}
