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

import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class PDA {

    public static PDA create(Grammar grammar, int start) {
        List<Shift> allShifts;
        PDA pda;

        pda = new PDA(grammar, start);
        pda.calcLR0();
        allShifts = new ArrayList<Shift>();
        pda.prepare(allShifts);
        pda.calc(allShifts);
        pda.finish();
        return pda;
    }

    /** start symbol */
    public final int start;

    // environment for computation
    public final Grammar grammar;      // grammar
    public final IntBitSet nullable;  // nullable symbols
    public final List<State> states;      // states built so far


    private State end;

    //--

    public PDA(Grammar grammar, int start) {
        this.grammar = grammar;
        this.nullable = new IntBitSet();
        this.states = new ArrayList<State>();
        this.start = start;
        this.grammar.addNullable(nullable);
    }

    /**
     * Pseudo-symbol, indicates end-of-file (or an empty word if lookahead is seen as a set of words of length <= 1)
     */
    public int getEofSymbol() {
        return grammar.getSymbolCount();
    }

    //-------------------------------------------------------------------

    private void calcLR0() {
        int i;
        State state;

        states.add(State.create(this, start));
        // note: the loop grows its upper bound
        for (i = 0; i < states.size(); i++) {
            state = getState(i);
            state.expand(this);
        }
        end = getState(0).createShifted(this, start);
        end.createShifted(this, getEofSymbol());
    }

    private void prepare(List<Shift> shifts) {
        int i, max;
        State state;

        max = states.size();
        for (i = 0; i < max; i++) {
            state = getState(i);
            state.prepare(this, shifts);
        }
    }

    private void calc(List<Shift> shifts) {
        int i, max;
        Shift sh;
        List<Shift> stack;

        max = shifts.size();
        for (i = 0; i < max; i++) {
            sh = shifts.get(i);
            sh.initReadCalc();
        }
        stack = new ArrayList<Shift>();
        for (i = 0; i < max; i++) {
            sh = shifts.get(i);
            sh.digraph(stack);
        }
        for (i = 0; i < max; i++) {
            sh = shifts.get(i);
            sh.saveReadCalc();
            sh.initFollowCalc();
        }

        stack = new ArrayList<Shift>();
        for (i = 0; i < max; i++) {
            sh = shifts.get(i);
            sh.digraph(stack);
        }
        for (i = 0; i < max; i++) {
            sh = shifts.get(i);
            sh.saveFollowCalc();
        }
    }

    private void finish() {
        int i, max;
        State state;

        max = states.size();
        for (i = 0; i < max; i++) {
            state = getState(i);
            state.calcLookahead(this);
        }
    }

    //----------------------------------------------------------------


    public ParserTable createTable(int lastSymbol, ConflictHandler handler) throws GenericException {
        // the initial syntaxnode created by the start action is ignoed!
        ParserTable result;
        int i, max;
        int eof;

        max = states.size();
        eof = getEofSymbol();
        result = new ParserTable(0, max, lastSymbol + 1 /* +1 for EOF */, eof, grammar, null);
        for (i = 0; i < max; i++) {
            getState(i).addActions(result, handler);
        }
        result.addAccept(end.id, eof);
        return result;
    }

    //----------------------------------------------------------------

    public int size() {
        return states.size();
    }

    public State getState(int idx) {
        return states.get(idx);
    }


    /**
     * I'd like to implement toString instead, but memory consumption
     * is to high for large automatons.
     */
    public void print(Grammar grammar, PrintStream dest) {
        int i, max;

        max = states.size();
        for (i = 0; i < max; i++) {
            dest.println(getState(i).toString(this, grammar));
        }
    }
}
