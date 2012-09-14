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
