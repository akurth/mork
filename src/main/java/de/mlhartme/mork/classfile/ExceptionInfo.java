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

package de.mlhartme.mork.classfile;

import java.io.IOException;

public class ExceptionInfo {
    public int start;  // a pc
    public int end;    // a pc or code.size
    public int handler; // a pc
    public ClassRef type;

    public static final int SIZE = 8;

    public ExceptionInfo(int start, int end, int handler, ClassRef type) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.type = type;
    }

    public ExceptionInfo(Input src) throws IOException {
        src.requireCode();
        start = src.readIdx();
        end = src.readIdxOrLast();
        handler = src.readIdx();
        try {
            type = src.readClassRef();
        } catch (NullPointerException e) {
            type = null;  // default handler, called for any exception
        }
    }

    public void write(Output dest) throws IOException {
        dest.requireCode();
        dest.writeIdx(start);
        dest.writeIdxOrLast(end);
        dest.writeIdx(handler);
        try {
            dest.writeClassRef(type);
        } catch (NullPointerException e) {
            dest.writeU2(0);
        }
    }

    @Override
    public String toString() {
        return "start=" + start + " end=" + end +
            " handler=" + handler + " type=" + type;
    }
}
