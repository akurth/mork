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
package net.oneandone.mork.mapping;

import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.reflect.Constructor;
import net.oneandone.mork.reflect.Field;
import net.oneandone.mork.reflect.Method;
import net.oneandone.mork.reflect.Selection;

/**
 * Java class with a name.
 */
public class Import {
    public final String name;
    public final Class target;

    public static final String NO_CLASS = "no such class";
    public static final String NO_CONSTRUCTOR = "no public constructor";
    public static final String NO_MEMBER = "no such method or field";

    public Import(String name, Class target) {
        this.name = name;
        this.target = target;
    }

    /**
     * @param name may be null
     */
    public static Import create(String packageName, String className, String name) throws GenericException {
        String fullName;
        Class cls;

        if (name == null) {
            name = className;
        }
        fullName = packageName + "." + className;
        cls = new ClassRef(fullName).lookup();
        if (cls == null) {
            throw new GenericException(NO_CLASS, fullName);
        }
        return new Import(name, cls);
    }


    /**
     * Gets a String representation for this Reference
     *.
     * @return the String representation
     */
    @Override
    public String toString() {
        return "reference " + name + ":" + target;
    }

    public Selection lookup(String member) throws GenericException {
        Selection selection;
        Field f;

        selection = Method.forName(target, member);
        f = Field.forName(target, member);
        if (f != null) {
            selection = selection.add(new Selection(f));
        }
        if (selection.isEmpty()) {
            throw new GenericException(NO_MEMBER, name + "." + member);
        }
        return selection;
    }

    public Selection getConstructors() throws GenericException {
        Selection selection;

        selection = Constructor.forClass(target);
        if (selection.isEmpty()) {
            throw new GenericException(NO_CONSTRUCTOR, name);
        }
        return selection;
    }
}
