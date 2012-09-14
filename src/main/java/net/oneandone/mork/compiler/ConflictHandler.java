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
package net.oneandone.mork.compiler;

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.parser.Conflict;
import net.oneandone.mork.parser.Parser;
import net.oneandone.mork.parser.ParserTable;
import net.oneandone.mork.pda.Item;
import net.oneandone.mork.pda.State;

import java.util.ArrayList;
import java.util.List;

public class ConflictHandler {
    private final Grammar grammar;
    private final List<Conflict> conflicts;
    private final List<ConflictResolver> resolvers;

    public ConflictHandler(Grammar grammar) {
        this.grammar = grammar;
        this.conflicts = new ArrayList<Conflict>();
        this.resolvers = new ArrayList<ConflictResolver>();
    }

    public char resolve(int stateId, State state, int symbol, char oldAction, char reduceAction) {
        int operand;

        switch (ParserTable.getAction(oldAction)) {
            case Parser.SHIFT:
                conflicts.add(new Conflict("shift-reduce", stateId, state, symbol, oldAction, reduceAction));
                return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
            case Parser.REDUCE:
                return reduceReduceConflict(stateId, state, symbol, -1, oldAction, reduceAction);
            case Parser.SPECIAL:
                operand = ParserTable.getOperand(oldAction);
                switch (operand & 0x03) {
                    case Parser.SPECIAL_CONFLICT:
                        return reduceReduceConflict(stateId, state, symbol, operand >> 2, reduceAction);
                    case Parser.SPECIAL_ERROR:
                        return oldAction;
                    default:
                        throw new IllegalStateException();
                }
            default:
                throw new IllegalStateException();
        }
    }

    public char reduceReduceConflict(int stateId, State state, int symbol, int resolverNo, int ... newReduceActions) {
        List<Item> items;
        List<Line> lines;
        Line[] array;
        Line line;
        Line conflicting;
        ConflictResolver resolver;
        Item i;
        List<Integer> allReduceActions;

        items = new ArrayList<Item>();
        allReduceActions = new ArrayList<Integer>();
        if (resolverNo != -1) {
            resolver = resolvers.get(resolverNo);
            for (Line l : resolver.lines) {
                i = state.getReduceItem(ParserTable.getOperand(l.action));
                if (!items.contains(i)) {
                    items.add(i);
                    allReduceActions.add(l.action);
                }
            }
        }
        for (int reduceAction : newReduceActions) {
            i = state.getReduceItem(ParserTable.getOperand(reduceAction));
            if (!items.contains(i)) {
                items.add(i);
                allReduceActions.add(reduceAction);
            }
        }
        lines = new ArrayList<Line>();
        for (Item item : items) {
            for (int[] terminals : item.lookahead.follows(symbol)) {
                line = new Line(terminals, ParserTable.createValue(Parser.REDUCE, item.getProduction()));
                conflicting = Line.lookupTerminals(lines, line.terminals);
                if (conflicting != null) {
                    conflicts.add(new Conflict("reduce-reduce", stateId, state, symbol, toArray(allReduceActions)));
                    return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
                }
                lines.add(line);
            }
        }
        array = new Line[lines.size()];
        lines.toArray(array);
        resolver = new ConflictResolver(array);
        if (resolverNo == -1) {
            resolvers.add(resolver);
            resolverNo = resolvers.size() - 1;
        } else {
            resolvers.set(resolverNo, resolver);
        }
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_CONFLICT | (resolverNo << 2));
    }

    private static int[] toArray(List<Integer> lst) {
        int[] result;

        result = new int[lst.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = lst.get(i);
        }
        return result;
    }

    public int conflicts() {
        return conflicts.size();
    }

    public ConflictResolver[] report(Output output, Grammar grammar) throws GenericException {
        if (conflicts.size() > 0) {
            for (Conflict conflict : conflicts ) {
                output.error("TODO", conflict.toString(grammar));
            }
            throw new GenericException("aborted with conflicts");
        }
        return resolvers.toArray(new ConflictResolver[resolvers.size()]);
    }

    public int resolvers() {
        return resolvers.size();
    }
}
