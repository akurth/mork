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
