/*
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
