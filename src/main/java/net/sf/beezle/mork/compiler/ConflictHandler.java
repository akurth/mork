package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.Conflict;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.pda.Item;
import net.sf.beezle.mork.pda.PDA;

import java.util.ArrayList;
import java.util.List;

public class ConflictHandler {
    private final PDA pda;
    private final List<Conflict> conflicts;
    private final List<LookaheadConflictResolver> resolvers;

    public ConflictHandler(PDA pda) {
        this.pda = pda;
        this.conflicts = new ArrayList<Conflict>();
        this.resolvers = new ArrayList<LookaheadConflictResolver>();
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

    public int conflict(int state, int symbol, int actionA, int actionB) {
        conflicts.add(new Conflict(pda.get(state), symbol, actionA, actionB));
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
    }

    public LookaheadConflictResolver[] report(Output output, Grammar grammar) throws GenericException {
        if (conflicts.size() > 0) {
            for (Conflict conflict : conflicts ) {
                output.error("TODO", conflict.toString(grammar));
            }
            throw new GenericException("aborted with conflicts");
        }
        return resolvers.toArray(new LookaheadConflictResolver[resolvers.size()]);
    }

    public int conflicts() {
        return resolvers.size();
    }
}
