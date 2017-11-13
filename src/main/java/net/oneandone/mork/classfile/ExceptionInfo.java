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

public class ExceptionInfo {
    public int start;  // a pc
    public int end;    // a pc or code.size
    public int handler; // a pc
    public ClassRef type;

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
