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

public abstract class Expression {
    public static final int NONE = -1;
    public static final int BOOL = 0;
    public static final int INT = 1;
    public static final int STR = 2;

    // operations, both unary and binary
    public static final int ADD = 0;
    public static final int SUB = 1;
    public static final int MUL = 2;
    public static final int DIV = 3;
    public static final int REM = 4;

    public static final int AND = 5;
    public static final int OR =  6;
    public static final int NOT = 7;

    public static final int EQ = 8;
    public static final int NE = 9;

    public static final int LT = 10;
    public static final int GT = 11;
    public static final int LE = 12;
    public static final int GE = 13;


    public abstract int getType();
    public abstract Object eval();

    // eval and unwrap
    public boolean evalBool() {
        return ((Boolean) eval()).booleanValue();
    }
    public int evalInt() {
        return ((Integer) eval()).intValue();
    }
    public String evalString() {
        return (String) eval();
    }
}
