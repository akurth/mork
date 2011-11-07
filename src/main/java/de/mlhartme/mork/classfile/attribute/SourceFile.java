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

import de.mlhartme.mork.classfile.Input;
import de.mlhartme.mork.classfile.Output;

public class SourceFile extends Attribute {
    public static final String NAME = "SourceFile";

    public final String file;

    public SourceFile(String fileInit) {
        super(NAME);

        file = fileInit;
    }

    public SourceFile(Input src) throws IOException {
        super(NAME);

        int len;

        len = src.readU4();
        if (len != 2) {
            throw new RuntimeException("SourceFile attribute of length "
                                       + len);
        }
        file = src.readUtf8();
    }

    @Override
    public void write(Output dest) throws IOException {
        dest.writeUtf8(name);
        dest.writeU4(2);
        dest.writeUtf8(file);
    }

    @Override
    public String toString() {
        return name + " attribute " + file;
    }
}
