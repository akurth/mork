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

package net.sf.beezle.mork.scanner;

import net.sf.beezle.sushi.util.IntBitSet;

import java.io.IOException;
import java.io.Reader;

/**
 * A token stream, input for parsers.
 */
public class Scanner {
    public static final int EOF = -1;
    public static final int ERROR = -2;

    // this should be a value with a short representation in a Utf8 String
    // because it is saved as part of a String
    public static final int ERROR_PC = 1;

    // the last value with a two-byte representation in utf8
    // (this constant is less important than ERROR_PC because most states
    // are end states; in my Java grammar 40-non-end vs. 260 end states)
    public static final int NO_TERMINAL = 0x07ff;

    /** start state */
    private final int start;

    /** number of modes */
    private final int modeCount;

    /** see ScannerFactory for a description */
    private final char[] table;

    private final Buffer src;

    public Scanner(int start, int modeCount, char[] table, Position pos, Reader reader) {
        this.start = start;
        this.modeCount = modeCount;
        this.table = table;
        this.src = new Buffer();
        src.open(pos, reader);
    }

    /** assigns the position of the last terminal returned by eat. */
    public void getPosition(Position result) {
        src.getPosition(result);
    }

    /** returns the text of the last terminal returned by eat. */
    public String getText() {
        return src.createString();
    }

    /**
     * Scans the next terminal.
     * @return terminal or ERROR or EOF
     */
    public int next(int mode) throws IOException {
        src.eat();
        return scan(mode);
    }


    public int find(int mode, IntBitSet terminals) throws IOException {
        int ofs;
        int terminal;

        ofs = src.getEndOfs();
        do {
            terminal = scan(mode);
        } while (terminal != EOF && !terminals.contains(terminal));
        src.resetEndOfs(ofs);
        return terminal;
    }

    private int scan(int mode) throws IOException {
        int pc;    // idx in table
        int c;
        int terminal;
        int matchedTerminal;
        int matchedEndOfs;
        int endOfs;

        matchedTerminal = ERROR;
        matchedEndOfs = 0;
        endOfs = src.getEndOfs();
        pc = start;
        do {
            terminal = table[pc + mode];
            pc += modeCount;
            if (terminal != NO_TERMINAL) {
                matchedTerminal = terminal;
                matchedEndOfs = endOfs;
            }
            c = src.read();
            if (c == Scanner.EOF) {
                src.resetEndOfs(matchedEndOfs);
                return matchedTerminal == ERROR ? EOF : matchedTerminal;
            }
            endOfs++;
            while (c > table[pc]) {
                pc += 2;
            }
            pc = table[pc + 1];
        } while (pc != ERROR_PC);
        src.resetEndOfs(matchedEndOfs);
        return matchedTerminal;
    }
}
