package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;

public class Resolution {
    private final ResolutionLine[] lines;

    public Resolution(ResolutionLine left, ResolutionLine right) {
        lines = new ResolutionLine[] { left, right };
    }

    public ConflictResolver resolve(Grammar grammar, int actionA, int actionB) {
        ResolutionLine left;
        ResolutionLine right;

        if (ParserTable.getAction(actionA) == Parser.REDUCE && ParserTable.getAction(actionB) == Parser.REDUCE) {
            left = find(grammar, actionA);
            if (left != null) {
                right = find(grammar, actionB);
                if (right != null) {
                    return new ManualConflictResolver(left.terminals, actionA, right.terminals, actionB);
                }
            }
        }
        return null;
    }

    public ResolutionLine find(Grammar grammar, int action) {
        for (ResolutionLine line : lines) {
            if (line.symbol == grammar.getLeft(ParserTable.getOperand(action))) {
                return line;
            }
        }
        return null;
    }
}
