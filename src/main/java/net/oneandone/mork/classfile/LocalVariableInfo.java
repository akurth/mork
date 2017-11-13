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
package net.oneandone.mork.classfile;

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
