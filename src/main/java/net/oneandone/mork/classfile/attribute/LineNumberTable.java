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
package net.oneandone.mork.classfile.attribute;

import net.oneandone.mork.classfile.Input;
import net.oneandone.mork.classfile.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineNumberTable extends Attribute {
    public static final String NAME = "LineNumberTable";

    public final List<Integer> pcs;
    public final List<Integer> lines;

    public LineNumberTable() {
        super(NAME);

        pcs = new ArrayList<Integer>();
        lines = new ArrayList<Integer>();
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
            pcs.add(src.readIdx());
            lines.add((int) src.readU2());
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
            dest.writeIdx(pcs.get(i));
            dest.writeU2(lines.get(i));
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
