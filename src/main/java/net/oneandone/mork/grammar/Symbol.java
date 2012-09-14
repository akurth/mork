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

/**
 * A stupid data container for Grammar.
 */
public class Symbol {
    /** productions for this symbol */
    public final int[] alternatives;

    /** productions using this symbol. */
    public final int[] users;

    /** ofsets in the using productions. */
    public final int[][] userOfs;

    public Symbol(int[] alternatives, int[] users, int[][] userOfs) {
        this.alternatives = alternatives;
        this.users = users;
        this.userOfs = userOfs;
    }
}
