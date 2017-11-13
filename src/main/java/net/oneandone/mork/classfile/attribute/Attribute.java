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
package net.oneandone.mork.classfile.attribute;

import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.classfile.Input;
import net.oneandone.mork.classfile.Output;

import java.io.IOException;

public abstract class Attribute {
    public final String name;

    protected Attribute(String nameInit) {
        name = nameInit;
    }

    public static Attribute create(Input src) throws IOException {
        String name;

        name = src.readUtf8();
        if (name.equals("Code")) {
            return new Code(src);
        } else if (name.equals("ConstantValue")) {
            return new ConstantValue(src);
        } else if (name.equals("Exceptions")) {
            return new Exceptions(src);
        } else if (name.equals("InnerClasses")) {
            return new InnerClasses(src);
        } else if (name.equals("Synthetic")) {
            return new Synthetic(src);
        } else if (name.equals("SourceFile")) {
            return new SourceFile(src);
        } else if (name.equals("LineNumberTable")) {
            return new LineNumberTable(src);
        } else if (name.equals("LocalVariableTable")) {
            return new LocalVariableTable(src);
        } else if (name.equals("Deprecated")) {
            return new Deprecated(src);
        } else {
            return new Blackbox(name, src);
        }
    }

    public abstract void write(Output dest) throws IOException;
}
