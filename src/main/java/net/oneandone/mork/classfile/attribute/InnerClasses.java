/**
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
package net.oneandone.mork.classfile.attribute;

import net.oneandone.mork.classfile.InnerClassesInfo;
import net.oneandone.mork.classfile.Input;
import net.oneandone.mork.classfile.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InnerClasses extends Attribute {
    public static final String NAME = "InnerClasses";

    public final List<InnerClassesInfo> infos;

    public InnerClasses() {
        super(NAME);

        infos = new ArrayList<InnerClassesInfo>();
    }

    public InnerClasses(Input src) throws IOException {
        this();

        int i;
        int len;
        int count;

        len = src.readU4();
        count = src.readU2();
        if (2 + count * InnerClassesInfo.SIZE != len) {
            throw new RuntimeException(NAME + ": illegal length: " +
                                       "count=" + count + " len=" + len);
        }
        for (i = 0; i < count; i++) {
            infos.add(new InnerClassesInfo(src));
        }
    }

    @Override
    public void write(Output dest) throws IOException {
        int i;
        int len;
        InnerClassesInfo info;
        int start;

        dest.writeUtf8(name);
        start = dest.writeSpace(4);
        len = infos.size();
        dest.writeU2(infos.size());
        for (i = 0; i < len; i++) {
            info = (InnerClassesInfo) infos.get(i);
            info.write(dest);
        }
        dest.writeFixup(start, dest.getGlobalOfs() - (start + 4));
    }
}
