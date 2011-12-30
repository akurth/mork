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

import net.sf.beezle.mork.compiler.ConflictHandler;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.parser.ParserTable;

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

    public abstract List<? extends BaseItem> allItems();

    public void addActions(ParserTable result, ConflictHandler handler) {
        int terminal;

        for (S sh : shifts) {
            result.addShift(id, sh.symbol, sh.end.id, handler);
        }
        for (R r : reduces) {
            for (terminal = r.lookahead.first(); terminal != -1; terminal = r.lookahead.next(terminal)) {
                result.addReduce(id, terminal, r.production, handler);
            }
        }
    }

    public S lookupShift(int symbol) {
        for (S shift : shifts) {
            if (shift.symbol == symbol) {
                return shift;
            }
        }
        return null;
    }

    public String toString(Grammar grammar) {
        StringBuilder result;

        result = new StringBuilder();
        result.append("\n------------------------------\n");
        result.append("[state " + id + "]\n");
        for (BaseItem item : allItems()) {
            result.append(item.toString(grammar));
        }
        result.append('\n');
        for (BaseShift sh : shifts) {
            result.append(sh.toString(grammar.getSymbolTable()));
        }
        result.append('\n');
        for (BaseReduce r : reduces) {
            result.append(r.toString(grammar));
        }
        result.append("\n");
        return result.toString();
    }
}
