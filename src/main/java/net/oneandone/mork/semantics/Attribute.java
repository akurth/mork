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
package net.oneandone.mork.semantics;

import net.oneandone.mork.misc.StringArrayList;

import java.util.List;

/**
 * An attribute is the type of an attribute instance. Immutable,
 * one instance for each attribute - equal by pointer comparison.
 */

public class Attribute {
    /** An arbitrary value the user might wish to search for. May be null. */
    public final String name;

    public final int symbol;

    public final Type type;

    //--

    public Attribute(int symbol, String name, Type type) {
        this.name = name;
        this.symbol = symbol;
        this.type = type;
    }

    /**
     * Creates Attribute with Object type. Usefull for test cases.
     */
    public Attribute(int symbol, String name) {
        this(symbol, name, new Type(Object.class));
    }

    public Attribute(Attribute orig) {
        this.name = orig.name;
        this.symbol = orig.symbol;
        this.type = orig.type;
    }

    public static Attribute find(List<Attribute> attrs, int symbol, String name) {
        int i;
        int max;
        Attribute attr;

        max = attrs.size();
        for (i = 0; i < max; i++) {
            attr = attrs.get(i);
            if (attr.symbol == symbol && name.equals(attr.name)) {
                return attr;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return symbol;
    }

    @Override
    public String toString() {
        return "symbol=" + symbol + ", name=" + name + ", " + type;
    }

    public String toString(StringArrayList symbolTable) {
        return symbolTable.get(symbol) + "." + name;
    }
}
