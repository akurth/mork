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
package net.oneandone.mork.semantics;

import net.oneandone.mork.misc.StringArrayList;

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
