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
import java.util.ArrayList;
import java.util.List;

import de.mlhartme.mork.classfile.Input;
import de.mlhartme.mork.classfile.LocalVariableInfo;
import de.mlhartme.mork.classfile.Output;

public class LocalVariableTable extends Attribute {
    public static final String NAME = "LocalVariableTable";

    public final List infos;

    public LocalVariableTable() {
        super(NAME);

        infos = new ArrayList();
    }

    public LocalVariableTable(Input src) throws IOException {
        this();

        int i;
        int len;
        int count;

        src.requireCode();
        len = src.readU4();
        count = src.readU2();
        if (2 + count * LocalVariableInfo.SIZE != len) {
            throw new RuntimeException(NAME + ": illegal length: " +
                                       "count=" + count + " len=" + len);
        }
        for (i = 0; i < count; i++) {
            infos.add(new LocalVariableInfo(src));
        }
    }

    @Override
    public void write(Output dest) throws IOException {
        int i;
        int max;
        LocalVariableInfo info;
        int start;

        dest.requireCode();
        dest.writeUtf8(name);
        start = dest.writeSpace(4);
        max = infos.size();
        dest.writeU2(infos.size());
        for (i = 0; i < max; i++) {
            info = (LocalVariableInfo) infos.get(i);
            info.write(dest);
        }
        dest.writeFixup(start, dest.getGlobalOfs() - (start + 4));
    }

    @Override
    public String toString() {
        StringBuilder result;
        int i, max;

        result = new StringBuilder();
        result.append("LocalTableTable\n");
        max = infos.size();
        for (i = 0; i < max; i++) {
            result.append('\t');
            result.append(infos.get(i).toString());
            result.append('\n');
        }
        return result.toString();
    }
}
