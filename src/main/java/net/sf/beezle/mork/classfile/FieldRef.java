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

import java.lang.reflect.Field;

public class FieldRef extends Reference implements Constants {
    public final ClassRef owner;
    public final String name;
    public final ClassRef type;

    public FieldRef(ClassRef owner, String name, ClassRef type) {
        this.owner = owner;
        this.name = name;
        this.type = type;
    }

    public FieldRef(Field field) {
        owner = new ClassRef(field.getDeclaringClass());
        name = field.getName();
        type = new ClassRef(field.getType());
    }

    @Override
    public ClassRef getOwner() {
        return owner;
    }

    @Override
    public FieldDef lookup(Repository repository) throws ResolveException {
        return lookup((ClassDef) owner.resolve(repository), repository);
    }
    
    private FieldDef lookup(ClassDef def, Repository repository) throws ResolveException {
        FieldDef field;
        
        field = def.lookupField(name);
        if (field != null) {
            return field;
        }
        
        // order doesn't matter - Javac rejects ambiguous references
        for (ClassRef next : def.interfaces) {
            field = lookup((ClassDef) next.resolve(repository), repository);
            if (field != null) {
                return field;
            }
        }
        if (def.superClass != null) {
            return lookup((ClassDef) def.superClass.resolve(repository), repository);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        FieldRef ref;

        if (!(obj instanceof FieldRef)) {
            return false;
        }
        ref = (FieldRef) obj;
        return owner.equals(ref.owner)
            && name.equals(ref.name)
            && type.equals(ref.type);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return type + " " + owner + "." + name;
    }
}
