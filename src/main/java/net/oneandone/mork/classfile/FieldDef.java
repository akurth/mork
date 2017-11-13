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

import net.oneandone.mork.classfile.attribute.Attribute;

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
