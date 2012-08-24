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
package net.sf.beezle.mork.classfile;

// not public
class NameAndType {
    /** field or method name */
    public final String name;

    /** field or method descriptor */
    public final String descriptor;

    public NameAndType(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    public NameAndType(String name, FieldRef ref) {
        this(name, ref.type.toFieldDescriptor());
    }

    public NameAndType(String name, MethodRef ref) {
        this(name, ref.toDescriptor());
    }

    @Override
    public boolean equals(Object obj) {
        NameAndType nt;

        if (!(obj instanceof NameAndType)) {
            return false;
        }
        nt = (NameAndType) obj;
        return name.equals(nt.name) && descriptor.equals(nt.descriptor);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
