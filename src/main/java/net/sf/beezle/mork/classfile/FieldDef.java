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

package net.sf.beezle.mork.classfile;

import net.sf.beezle.mork.classfile.attribute.Attribute;

import java.io.IOException;
import java.util.Set;

public class FieldDef extends Definition {
    public final Set<Access> accessFlags;
    public final String name;
    public final ClassRef type;
    public Attribute[] attributes;

    public FieldDef(Set<Access> accessFlags, String name, ClassRef type) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.type = type;
        this.attributes = new Attribute[0];
    }

    public FieldDef(Input src) throws IOException {
        int i;
        String descriptor;

        accessFlags = Access.fromFlags(src.readU2(), false);
        name = src.readUtf8();
        descriptor = src.readUtf8();
        type = ClassRef.forFieldDescriptor(descriptor);
        attributes = new Attribute[src.readU2()];
        for (i = 0; i < attributes.length; i++) {
            attributes[i] = Attribute.create(src);
        }
    }

    public void write(Output dest) throws IOException {
        int i;

        dest.writeU2(Access.toFlags(accessFlags));
        dest.writeUtf8(name);
        dest.writeUtf8(type.toFieldDescriptor());
        dest.writeU2(attributes.length);
        for (i = 0; i < attributes.length; i++) {
            attributes[i].write(dest);
        }
    }

    public FieldRef reference(ClassRef owner) {
        return new FieldRef(owner, name, type);
    }

    @Override
    public String toString() {
        return Access.toPrefix(accessFlags) + type + " " + name;
    }
}
