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
package net.oneandone.mork.regexpr;

import java.io.Serializable;

/**
 * Regular Expression. All derived classes shall be immutable, it has
 * to be safe to share instances. Anything that can read from a buffer
 * and that can be visited is considered a regular expression.
 */

public abstract class RegExpr implements Serializable {
    /**
     * Visit this expressions and its sub-expression and perform
     * some action.
     */
    public abstract Object visit(Action action) throws ActionException;
}
