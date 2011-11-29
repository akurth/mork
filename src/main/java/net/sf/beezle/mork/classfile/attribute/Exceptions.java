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

package net.sf.beezle.mork.classfile.attribute;

import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Input;
import net.sf.beezle.mork.classfile.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Exceptions extends Attribute {
    public static final String NAME = "Exceptions";
    public final List<ClassRef> exceptions;

    public Exceptions() {
        super(NAME);

        exceptions = new ArrayList<ClassRef>();
    }

    public Exceptions(Input src) throws IOException {
        this();

        int i;
        int len;
        int count;

        len = src.readU4();
        count = src.readU2();
        if (2 + count * 2 != len) {
            throw new RuntimeException("illegal exceptions attribute");
        }
        for (i = 0; i < count; i++) {
            exceptions.add(src.readClassRef());
        }
    }

    @Override
    public void write(Output dest) throws IOException {
        int i;
        int count;
        int start;

        dest.writeUtf8(name);
        start = dest.writeSpace(4);
        count = exceptions.size();
        dest.writeU2(count);
        for (i = 0; i < count; i++) {
            dest.writeClassRef((ClassRef) exceptions.get(i));
        }
        dest.writeFixup(start, dest.getGlobalOfs() - (start + 4));
    }

    @Override
    public String toString() {
        StringBuilder result;
        int i, len;

        result = new StringBuilder();
        result.append(NAME);
        result.append(" attrib\n");
        len = exceptions.size();
        for (i = 0; i < len; i++) {
            result.append('\t');
            result.append(exceptions.get(i).toString());
            result.append('\t');
        }
        return result.toString();
    }
}
