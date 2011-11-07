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

package compiler;

public interface Operator {
    int ADD =  0;
    int SUB =  1;
    int MUL =  2;
    int DIV =  3;

    int AND =  5;
    int OR  =  6;

    int EQ  =  8;
    int NE  =  9;
    int LT  = 10;
    int GT  = 11;
    int LE  = 12;
    int GE  = 13;
}
