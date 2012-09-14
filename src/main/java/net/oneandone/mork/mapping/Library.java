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

import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.reflect.Selection;

/**
 * A set of types.
 * Purpose:
 *  o abbreviate function names
 *  o declare external dependencies
 *
 * Remarks
 * + good error messages
 * + extendable, e.g. modifier
 * - allows renaming in both the library and the translation section ...
 * - much typing
 *
 * Types must have a name that can be used for attribute names;
 * otherwise, I could define anonymous types together with their symbols.
 *
 * TODO:
 * o check for multiple definitions.
 * o move into reflect package? merge Type and Selection?
 */
public class Library {
    private final Import[] body;

    public Library(Import[] body) {
        this.body = body;
    }

    public static final String NO_CLASS = "no such class";

    public Selection lookupClass(String name) throws GenericException {
        Import ref;

        ref = lookupRaw(name);
        return ref.getConstructors();
    }

    public Selection lookupMember(String name, String member) throws GenericException {
        Import imp;

        imp = lookupRaw(name);
        return imp.lookup(member);
    }

    private Import lookupRaw(String name) throws GenericException {
        int i;

        for (i = 0; i < body.length; i++) {
            if (body[i].name.equals(name)) {
                return body[i];
            }
        }
        throw new GenericException(NO_CLASS, name);
    }
}
