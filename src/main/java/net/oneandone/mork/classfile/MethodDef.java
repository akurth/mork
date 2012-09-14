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

import net.oneandone.mork.classfile.attribute.Attribute;
import net.oneandone.mork.classfile.attribute.Exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MethodDef extends Definition {
    public Set<Access> accessFlags;
    public String name;
    public ClassRef[] argumentTypes;
    public ClassRef returnType;
    public final List<Attribute> attributes;

    public MethodDef() {
        attributes = new ArrayList<Attribute>();
    }

    public MethodDef(Input src) throws IOException {
        this();

        int i, max;
        String descriptor;

        accessFlags = Access.fromFlags(src.readU2(), false);
        name = src.readUtf8();
        descriptor = src.readUtf8();
        argumentTypes = MethodRef.forArgumentTypes(descriptor);
        returnType = MethodRef.forReturnType(descriptor);
        max = src.readU2();
        for (i = 0; i < max; i++) {
            attributes.add(Attribute.create(src));
        }
    }

    public void write(Output dest) throws IOException {
        int i, max;

        dest.writeU2(Access.toFlags(accessFlags));
        dest.writeUtf8(name);
        dest.writeUtf8(MethodRef.toDescriptor(argumentTypes, returnType));
        max = attributes.size();
        dest.writeU2(max);
        for (i = 0; i < max; i++) {
            ((Attribute) attributes.get(i)).write(dest);
        }
    }

    public List<ClassRef> getExceptions() {
        for (Attribute a : attributes) {
            if (a instanceof Exceptions) {
                return ((Exceptions) a).exceptions;
            }
        }
        return new ArrayList<ClassRef>();
    }

    public Code getCode() {
        for (Attribute a : attributes) {
            if (a instanceof Code) {
                return (Code) a;
            }
        }
        return null;
    }

    public MethodRef reference(ClassRef owner, boolean ifc) {
        return new MethodRef(owner, ifc, returnType, name, argumentTypes);
    }

    @Override
    public String toString() {
        StringBuilder buffer;
        int i, max;

        buffer = new StringBuilder(toSignatureString());
        buffer.append('\n');
        max = attributes.size();
        for (i = 0; i < max; i++) {
            buffer.append(attributes.get(i).toString());
            buffer.append('\n');
        }
        return buffer.toString();
    }

    public String toSignatureString() {
        boolean first;
        StringBuilder buffer;

        buffer = new StringBuilder(Access.toPrefix(accessFlags));
        buffer.append(returnType + " " + name + "(");
        for (int i = 0; i < argumentTypes.length; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(argumentTypes[i]);
        }
        buffer.append(")");
        first = true;
        for (ClassRef ref : getExceptions()) {
            if (first) {
                first = false;
                buffer.append(" throws ");
            } else {
                buffer.append(' ');
            }
            buffer.append(ref.toString());
        }
        return buffer.toString();
    }
}
