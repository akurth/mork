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

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.semantics.Ag;
import net.oneandone.mork.semantics.Attribute;
import net.oneandone.mork.semantics.NodeFactory;
import net.oneandone.mork.semantics.Type;

/**
 * Reference to internal constructor.
 */
public class Internal {
    /** reference to the internal constructor */
    private final int no;

    public static final String NO_SUCH_INTERNAL = "internal constructor not found";

    public Internal(int no) {
        this.no = no;
    }

    public Internal(String name) throws GenericException {
        no = NodeFactory.lookupAttribute(name);
        if (no == -1) {
            throw new GenericException(NO_SUCH_INTERNAL, name);
        }
    }

    public Attribute translate(int symbol, Grammar grm) {
        Attribute attr;
        Type type;

        type = new Type(NodeFactory.getDeclaration(no), Type.VALUE);
        attr = new Attribute(symbol, null, type); // null: anonymous attr
        return attr;
    }

    public void declare(Attribute attr, Ag sems) {
        sems.add(attr, no);
    }

    @Override
    public String toString() {
        return "internal constructor " + no;
    }
}
