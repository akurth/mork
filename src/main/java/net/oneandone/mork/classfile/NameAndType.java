/**
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
