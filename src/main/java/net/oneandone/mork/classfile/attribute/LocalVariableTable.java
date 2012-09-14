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
import net.oneandone.mork.classfile.LocalVariableInfo;
import net.oneandone.mork.classfile.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalVariableTable extends Attribute {
    public static final String NAME = "LocalVariableTable";

    public final List<LocalVariableInfo> infos;

    public LocalVariableTable() {
        super(NAME);

        infos = new ArrayList<LocalVariableInfo>();
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
