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
