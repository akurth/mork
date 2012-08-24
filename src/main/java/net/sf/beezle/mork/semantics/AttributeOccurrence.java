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
package net.sf.beezle.mork.semantics;

import net.sf.beezle.mork.misc.StringArrayList;

public class AttributeOccurrence {
    public final Attribute attr;

    /** references the respective symbol in the production; -1 for left hand side */
    public final int ofs;

    public AttributeOccurrence(Attribute attr, int ofs) {
        this.attr = attr;
        this.ofs = ofs;
    }

    @Override
    public String toString() {
        return attr.toString() + '[' + ofs + ']';
    }

    public String toString(StringArrayList symbolTable) {
        return symbolTable.getOrIndex(attr.symbol) + '[' + ofs + "]." + attr.hashCode();
    }

    public boolean matches(AttributeOccurrence ao) {
        return attr == ao.attr && ((ofs == -1 && ao.ofs != -1) || (ofs != -1 && ao.ofs == -1));
    }

    public boolean sameSymbolOccurrence(AttributeOccurrence ao) {
        return attr.symbol == ao.attr.symbol && ofs == ao.ofs;
    }

    @Override
    public boolean equals(Object obj) {
        AttributeOccurrence ao;

        if (obj instanceof AttributeOccurrence) {
            ao = (AttributeOccurrence) obj;
            return attr == ao.attr && ofs == ao.ofs;
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return ofs;
    }
}
