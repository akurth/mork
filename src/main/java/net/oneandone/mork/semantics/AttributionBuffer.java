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
import net.oneandone.mork.reflect.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Attribution function call buffer. Having the buffer simplifies creation
 * and optimization of AGs because things are not hard-wired and I don't
 * need a context to create an AttributionBuffer instance.
 */

// TODO: public final
public class AttributionBuffer {
    public final int production;

    // TODO: final
    public Function function;

    public final AttributeOccurrence result;
    private final List<AttributeOccurrence> args;

    public AttributionBuffer(AttributionBuffer orig) {
        this(orig.production, orig.function, orig.result);
        addAll(orig.args);
    }

    public AttributionBuffer(int production, Function function, AttributeOccurrence result) {
        if (function == null) {
            throw new IllegalArgumentException();
        }
        this.production = production;
        this.function = function;
        this.result = result;
        this.args = new ArrayList<AttributeOccurrence>();
    }

    public void add(AttributeOccurrence attr) {
        args.add(attr);
    }

    /**
     * @param args list of AttributeOccurrence objects
     */
    public void addAll(List<AttributeOccurrence> args) {
        for (AttributeOccurrence arg : args) {
            add(arg);
        }
    }

    public int getArgCount() {
        return args.size();
    }

    public AttributeOccurrence getArg(int i) {
        return args.get(i);
    }

    @Override
    public int hashCode() {
        return production;
    }

    @Override
    public String toString() {
        StringBuilder buf;
        int max;
        int i;

        buf = new StringBuilder();
        buf.append("prod ");
        buf.append(production);
        buf.append(':');
        buf.append(result);
        buf.append("  <==  (");
        max = args.size();
        for (i = 0; i < max; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(args.get(i).toString());
        }
        buf.append(')');
        if (function != null) {
            buf.append("  [");
            buf.append(function.toString());
            buf.append(']');
        }
        return buf.toString();
    }

    public void attrsToString(StringBuilder buffer, StringArrayList symbolTable) {
        int max;
        int i;

        buffer.append(function.getReturnType().getName());
        buffer.append(' ');
        buffer.append(result.toString(symbolTable));
        buffer.append(" = ");
        buffer.append(function.getName());
        buffer.append('(');
        max = args.size();
        for (i = 0; i < max; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(function.getParameterTypes()[i].getName());
            buffer.append(' ');
            buffer.append(args.get(i).toString(symbolTable));
        }
        buffer.append(')');
    }
}
