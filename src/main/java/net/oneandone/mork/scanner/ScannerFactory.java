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
package net.oneandone.mork.scanner;

import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.parser.ParserTable;
import net.oneandone.mork.regexpr.Range;
import net.oneandone.sushi.util.IntBitSet;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>An immutable FA. Instances are used for acual scanning. I would call
 * it FA if FA could be called FABuffer. But in this case, FABuilder
 * would become FABufferBuilder ...</p>
 *
 * <pre>
 * TODO
 * o binary search in TableFA?
 *  o Java Scanner needs up to 60 comparisons
 * </pre>
 */
public class ScannerFactory {
    public static final String SCANNER_TOO_BIG = "scanner too big";

    /** finite deterministic automaton */
    private final int start;

    private final int modeCount;

    /**
     * For every state that's not the error state
     *   * for each mode:
     *       end symbol - the terminal to return if this state is an end state; NO_TERMINAL otherwise
     *   * for each transition (aka range):
     *       last       - the last character of the range, or Scanner.ERROR_PC for errors
     *       pc         - state to goto when this range is matched
     *
     * Notes:
     * o I could get smaller constants in "last" by using values relative
     *   to the previous last -- but this slightly slows down the scanner
     *   and the Java scanner is just 3.5k smaller
     * o I could get smaller constants by using "first" instead of "last",
     *   but this saves just 300bytes in the Java scanner an scanning is
     *   slightly slower. It seems that lower ranges should be testet first.
     * o the Java ranges has states with more than 60 ranges ...
     */
    private final char[] table;

    //--

    public static ScannerFactory create(
        FA fa, int errorSi, ParserTable parserTable, IntBitSet whites, PrintWriter verbose, PrintWriter listing)
            throws GenericException {
        List modes;  // list of IntSets
        char[] table;

        if (listing != null) {
            listing.println("Scanner\n");
            listing.println(fa.toString());
        }
        if (verbose != null) {
            verbose.println("computing scanner modes");
        }
        modes = Modes.generate(fa, parserTable, whites, listing);
        if (verbose != null) {
            verbose.println("building table fa");
        }
        table = createTable(fa, errorSi, modes);
        return new ScannerFactory(fa.getStart(), modes.size(), table);
    }

    public static ScannerFactory createSimple(FA fa, int errorSi, IntBitSet terminals)
        throws GenericException {
        char[] data;
        List<IntBitSet> modes;

        modes = new ArrayList<IntBitSet>();
        modes.add(new IntBitSet(terminals));
        data = createTable(fa, errorSi, modes);
        return new ScannerFactory(fa.getStart(), 1, data);
    }

    private static char[] createTable(FA fa, int errorSi, List<IntBitSet> modes) throws GenericException {
        char[] table;
        int ti, si;
        int maxTi, maxSi;
        State state;
        Range range;
        int pc;
        int[] ofs;  // index by si; contains pc for this state
        int modeCount;

        modeCount = modes.size();

        // determin size and ofs
        maxSi = fa.size();
        ofs = new int[maxSi];
        pc = 0;
        for (si = 0; si < maxSi; si++) {
            if (si != errorSi) {
                ofs[si] = pc;
                pc += modeCount; // one terminal or NO_TERMINAL per mode
                pc += fa.get(si).size() * 2;
            }
        }
        if (pc >= Character.MAX_VALUE) {
            throw new GenericException(SCANNER_TOO_BIG);
        }

        // copy fa into table
        table = new char[pc];
        pc = 0;
        for (si = 0; si < maxSi; si++) {
            if (si != errorSi) {
                if (ofs[si] != pc) {
                    throw new IllegalStateException();
                }
                state = fa.get(si);
                for (IntBitSet mode : modes) {
                    table[pc] = getEndSymbol(fa, si, mode);
                    pc++;
                }

                // ranges
                maxTi = state.size();
                if (maxTi == 0) {
                    throw new RuntimeException();
                }
                for (ti = 0; ti < maxTi; ti++) {
                    range = state.getInput(ti);
                    table[pc] = range.getLast();
                    pc++;

                    if (state.getEnd(ti) == errorSi) {
                        table[pc] = Scanner.ERROR_PC;
                    } else {
                        // this cast is safe because max pc was tested above
                        table[pc] = (char) ofs[state.getEnd(ti)];
                    }
                    pc++;
                }
            }
        }
        if (pc != table.length) {
            throw new RuntimeException();
        }
        return table;
    }

    private static char getEndSymbol(FA fa, int si, IntBitSet modeSymbols) throws GenericException {
        Label label;
        int endSymbol;
        State state;

        if (!fa.isEnd(si)) {
            return Scanner.NO_TERMINAL;
        }
        state = fa.get(si);
        label = (Label) state.getLabel();
        endSymbol = label.getSymbol(modeSymbols);
        if (endSymbol == -1) {
            return Scanner.NO_TERMINAL;
        }
        if (endSymbol >= Scanner.NO_TERMINAL) {
            throw new GenericException(SCANNER_TOO_BIG);
        }
        return (char) endSymbol;
    }

    public ScannerFactory(int start, int modeCount, char[] table) {
        if (start == -1) {
            throw new IllegalArgumentException();
        }
        this.start = start;
        this.modeCount = modeCount;
        this.table = table;
    }

    public Scanner newInstance(Position pos, Reader src) {
        return new Scanner(start, modeCount, table, pos, src);
    }

    public int size() {
        return table.length;
    }
}
