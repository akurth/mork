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

package net.sf.beezle.mork.pda;

import net.sf.beezle.mork.grammar.Grammar;

import java.util.List;

public abstract class BaseState<S extends BaseShift, R extends BaseReduce> {
    public final int id;

    protected final List<S> shifts;

    /** List of Reduces. */
    protected final List<R> reduces;

    public BaseState(int id, List<S> shifts, List<R> reduces) {
        this.id = id;
        this.shifts = shifts;
        this.reduces = reduces;
    }

    public abstract String toString(Grammar grammar);
}
