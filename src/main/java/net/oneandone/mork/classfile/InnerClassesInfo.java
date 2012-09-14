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
package net.oneandone.mork.classfile;

import java.io.IOException;

public class InnerClassesInfo {
    public ClassRef inner;
    public ClassRef outer;
    public String name;
    public int flags;

    public static final int SIZE = 8;

    public InnerClassesInfo(Input src) throws IOException {
        try {
            inner = src.readClassRef();
        } catch (NullPointerException e) {
            inner = null;
        }
        try {
            outer = src.readClassRef();
        } catch (NullPointerException e) {
            outer = null;
        }
        try {
            name = src.readUtf8();
        } catch (NullPointerException e) {
            name = null;
        }
        flags = src.readU2();
    }

    public void write(Output dest) throws IOException {
        try {
            dest.writeClassRef(inner);
        } catch (NullPointerException e) {
            dest.writeU2(0);
        }
        try {
            dest.writeClassRef(outer);
        } catch (NullPointerException e) {
            dest.writeU2(0);
        }
        try {
            dest.writeUtf8(name);
        } catch (NullPointerException e) {
            dest.writeU2(0);
        }
        dest.writeU2(flags);
    }

}
