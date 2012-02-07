package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.Conflict;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.pda.Item;
import net.sf.beezle.mork.pda.PDA;
import net.sf.beezle.mork.pda.State;

import java.util.ArrayList;
import java.util.List;

public class ConflictHandler {
    private final PDA pda;
    private final List<Conflict> conflicts;
    private final List<ConflictResolver> resolvers;

    public ConflictHandler(PDA pda) {
        this.pda = pda;
        this.conflicts = new ArrayList<Conflict>();
        this.resolvers = new ArrayList<ConflictResolver>();
    }

    public char resolve(int state, int symbol, char oldAction, char reduceAction) {
        switch (ParserTable.getAction(oldAction)) {
            case Parser.SHIFT:
                conflicts.add(new Conflict("shift-reduce", pda.get(state), symbol, oldAction, reduceAction));
                return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
            case Parser.REDUCE:
                return reduceReduceConflict(state, symbol, oldAction, reduceAction);
            case Parser.SPECIAL:
                switch (ParserTable.getOperand(oldAction)) {
                    case Parser.SPECIAL_CONFLICT:
                         throw new UnsupportedOperationException("TODO");
                    case Parser.SPECIAL_ERROR:
                        return oldAction;
                    default:
                        throw new IllegalStateException();
                }
            default:
                throw new IllegalStateException();
        }
    }

    public char reduceReduceConflict(int stateId, int symbol, int ... reduceActions) {
        State state;
        List<Item> items;
        List<Line> lines;
        Line[] array;
        Line line;
        Line conflicting;

        state = pda.get(stateId);
        items = new ArrayList<Item>();
        for (int reduceAction : reduceActions) {
            items.add(state.getReduceItem(pda.grammar, ParserTable.getOperand(reduceAction)));
        }
        lines = new ArrayList<Line>();
        for (Item item : items) {
            for (int[] terminals : item.lookahead.follows(symbol)) {
                line = new Line(terminals, ParserTable.createValue(Parser.REDUCE, item.getProduction()));
                conflicting = Line.lookupTerminals(lines, line.terminals);
                if (conflicting != null) {
                    conflicts.add(new Conflict("reduce-reduce", state, symbol, reduceActions));
                    return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
                }
                lines.add(line);
            }
        }
        array = new Line[lines.size()];
        lines.toArray(array);
        resolvers.add(new ConflictResolver(array));
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_CONFLICT | ((resolvers.size() - 1) << 2));
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
