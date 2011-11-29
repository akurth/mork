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

package net.sf.beezle.mork.mapping;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.semantics.Ag;
import net.sf.beezle.mork.semantics.Attribute;
import net.sf.beezle.mork.semantics.NodeFactory;
import net.sf.beezle.mork.semantics.Type;

/**
 * Reference to internal constructor.
 */
public class Internal {
    /** reference to the internal constructor */
    private int no;

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

    public Attribute translate(int symbol, Grammar grm) throws GenericException {
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
