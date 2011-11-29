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

import net.sf.beezle.mork.classfile.Input;
import net.sf.beezle.mork.classfile.Output;

import java.io.IOException;

/** TODO: Replace these attribute with a proper representation */
public class Blackbox extends Attribute {
    public final byte[] info;

    public Blackbox(String nameInit, Input src) throws IOException {
        super(nameInit);

        int len;

        len = src.readU4();
        if ((len & 0xffff0000) != 0) {
            throw new RuntimeException("attribute to long: " + len);
        }
        info = new byte[len];
        src.read(info);
    }

    @Override
    public void write(Output dest) throws IOException {
        dest.writeUtf8(name);
        dest.writeU4(info.length);
        dest.write(info);
    }

    @Override
    public String toString() {
        return name + " attribute, len=" + info.length;
    }
}
