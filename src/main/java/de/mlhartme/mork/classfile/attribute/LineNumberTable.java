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

package de.mlhartme.mork.classfile.attribute;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import de.mlhartme.mork.classfile.Input;
import de.mlhartme.mork.classfile.Output;

public class LineNumberTable extends Attribute {
    public static final String NAME = "LineNumberTable";

    public final List pcs;
    public final List lines;

    public LineNumberTable() {
        super(NAME);

        pcs = new ArrayList();
        lines = new ArrayList();
    }

    public LineNumberTable(Input src) throws IOException {
        this();

        int i;
        int len;
        int count;

        src.requireCode();
        len = src.readU4();
        count = src.readU2();
        if (2 + count * 4 != len) {
            throw new RuntimeException("illegal LineNumberTable attribute");
        }
        for (i = 0; i < count; i++) {
            pcs.add(new Integer(src.readIdx()));
            lines.add(new Integer(src.readU2()));
        }
    }

    @Override
    public void write(Output dest) throws IOException {
        int i;
        int len;
        int start;

        dest.requireCode();
        dest.writeUtf8(name);
        start = dest.writeSpace(4);
        len = pcs.size();
        dest.writeU2(len);
        for (i = 0; i < len; i++) {
            dest.writeIdx(((Integer) pcs.get(i)).intValue());
            dest.writeU2(((Integer) lines.get(i)).intValue());
        }
        dest.writeFixup(start, dest.getGlobalOfs() - (start + 4));
    }

    @Override
    public String toString() {
        StringBuilder result;
        int i, len;

        result = new StringBuilder();
        result.append(NAME);
        result.append(" attribute\n");
        len = pcs.size();
        for (i = 0; i < len; i++) {
            result.append("  " + pcs.get(i) + " " + lines.get(i));
        }
        return result.toString();
    }
}
