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

public class Const extends Expression {
    private int type;
    private Object val;

    public Const(Object valInit) {
        val = valInit;
        if (val instanceof Boolean) {
            type = BOOL;
        } else if (val instanceof Integer) {
            type = INT;
        } else if (val instanceof String) {
            type = STR;
        } else {
            throw new RuntimeException("illegal constant type: "
                                       + val.getClass());
        }
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Object eval() {
        return val;
    }
}
