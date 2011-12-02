package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.parser.Conflict;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;

import java.util.ArrayList;
import java.util.List;

public class ConflictHandler {
    private final Grammar grammar;
    private final Resolution[] resolutions;
    private final List<Conflict> conflicts;
    private final List<ConflictResolver> resolvers;

    public ConflictHandler(Grammar grammar, Resolution ... resolutions) {
        this.grammar = grammar;
        this.resolutions = resolutions;
        this.conflicts = new ArrayList<Conflict>();
        this.resolvers = new ArrayList<ConflictResolver>();
    }

    public int resolve(int state, int symbol, int actionA, int actionB) {
        ConflictResolver resolver;

        for (Resolution resolution : resolutions) {
            resolver = resolution.resolve(grammar, actionA, actionB);
            if (resolver != null) {
                resolvers.add(resolver);
                return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_CONFLICT & ((resolvers.size() - 1) << 2));
            }
        }
        conflicts.add(new Conflict(state, symbol, actionA, actionB));
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
    }

    public ConflictResolver[] report(Output output, Grammar grammar) throws GenericException {
        if (conflicts.size() > 0) {
            for (Conflict conflict : conflicts ) {
                output.error("TODO", Syntax.LALR_CONFLICT + conflict.toString(grammar));
            }
            throw new GenericException("aborted with conflicts");
        }
        return resolvers.toArray(new ConflictResolver[resolvers.size()]);
    }
}
