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
package net.oneandone.mork.grammar;

import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.regexpr.RegExpr;

import java.io.Serializable;

/**
 * Defines a symbol by a redular expression.
 */

public class Rule implements Serializable {
    private int left;
    private RegExpr right;

    public Rule(int leftInit, RegExpr rightInit) {
        if (rightInit == null) {
            throw new NullPointerException();
        }
        left = leftInit;
        right = rightInit;
    }

    public int getLeft() {
        return left;
    }

    public RegExpr getRight() {
        return right;
    }

    public String toString(StringArrayList symTab) {
        return symTab.get(left) + "\t::= " + right.toString() + ";";
    }
}
