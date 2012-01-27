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

/** Position in a buffer. */
public class Position {
    /** Usually a URL */
    private String context;

    /** Starts at 1 */
    private int line;

    /** Starts at 1 */
    private int col;

    /** Starts at 0. Its redundant and may be removed. */
    private int ofs;

    public Position() {
        this(null);
    }

    public Position(String context) {
        this.context = context;
        this.line = 1;
        this.col = 1;
        this.ofs = 0;
    }

    public void set(String context, int line, int col, int ofs) {
        this.context = context;
        this.line = line;
        this.col = col;
        this.ofs = ofs;
    }

    public void set(Position arg) {
        set(arg.context, arg.line, arg.col, arg.ofs);
    }

    /** the indicated range has been passed by the scanner. */
    public void update(char[] data, int start, int end) {
        int i;

        for (i = start; i < end; i++) {
            if (data[i] == '\n') {
                col = 1;
                line++;
            } else {
                col++;
            }
        }
        ofs += (end - start);
    }

    public String getContext() {
        return context;
    }

    public int getOffset() {
        return ofs;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return col;
    }

    @Override
    public String toString() {
        if (context != null) {
            return context + ":" + line + ":" + col;
        } else {
            return line + ":" + col;
        }
    }
}
