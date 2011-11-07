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

public class LocalVariableInfo {
    public int start;  // code idx
    public int end;    // code idx
    public String name;
    public String descriptor;  // a field descriptor
    public int index;

    public static final int SIZE = 10;

    public LocalVariableInfo(Input src) throws IOException {
        start = src.readIdx();
        end = src.readEndIdxOrLast(start);
        name = src.readUtf8();
        descriptor = src.readUtf8();
        index = src.readU2();
    }

    public void write(Output dest) throws IOException {
        dest.writeIdx(start);
        dest.writeEndIdxOrLast(start, end);
        dest.writeUtf8(name);
        dest.writeUtf8(descriptor);
        dest.writeU2(index);
    }

    @Override
    public String toString() {
        return name + " " + descriptor + " " + start + " " + end;
    }
}
