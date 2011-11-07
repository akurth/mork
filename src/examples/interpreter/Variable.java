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

package interpreter;

public class Variable {
    private String name;
    private int type;
    private Object val;

    public Variable(int typeInit, String nameInit) {
        name = nameInit;
        type = typeInit;
        switch (type) {  // initialize
        case Expression.BOOL:
            val = new Boolean(false);
            break;
        case Expression.INT:
            val = new Integer(0);
            break;
        case Expression.STR:
            val = new String("");
            break;
        default:
            throw new RuntimeException("unkown type: " + type);
        }
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void set(Object obj) {
        val = obj;
    }

    public Object get() {
        return val;
    }
}
