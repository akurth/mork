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
