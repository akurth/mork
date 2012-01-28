package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.Conflict;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.pda.Item;

import java.util.ArrayList;
import java.util.List;

public class ConflictHandler {
    private final List<Conflict> conflicts;
    private final List<ConflictResolver> resolvers;

    public ConflictHandler() {
        this.conflicts = new ArrayList<Conflict>();
        this.resolvers = new ArrayList<ConflictResolver>();
    }

    public int resolve(int state, int symbol, int actionA, int actionB) {
        conflicts.add(new Conflict(state, symbol, actionA, actionB));
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
    }

    public void resolve(int state, int symbol, List<Item> items, ParserTable result) {
        List<Line> lines;
        Line[] array;
        Line line;
        Line conflicting;

        lines = new ArrayList<Line>();
        for (Item item : items) {
            for (int[] terminals : item.lookahead.follows(symbol)) {
                line = new Line(terminals, ParserTable.createValue(Parser.REDUCE, item.getProduction()));
                conflicting = Line.lookupTerminals(lines, line.terminals);
                if (conflicting != null) {
                    // lr(k) conflict
                    result.setTested(conflicting.action, state, symbol, this);
                    result.setTested(line.action, state, symbol, this);
                    return;
                }
                lines.add(line);
            }
        }
        array = new Line[lines.size()];
        lines.toArray(array);
        resolvers.add(new LookaheadConflictResolver(array));
        result.setTested(
                ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_CONFLICT | ((resolvers.size() - 1) << 2)),
                state, symbol, this);
    }

    public ConflictResolver[] report(Output output, Grammar grammar) throws GenericException {
        if (conflicts.size() > 0) {
            for (Conflict conflict : conflicts ) {
                output.error("TODO", Syntax.CONFLICT + conflict.toString(grammar));
            }
            throw new GenericException("aborted with conflicts");
        }
        return resolvers.toArray(new ConflictResolver[resolvers.size()]);
    }

    public int conflictCount() {
        return conflicts.size();
    }
}
