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

import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.classfile.Input;
import net.oneandone.mork.classfile.Output;

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
