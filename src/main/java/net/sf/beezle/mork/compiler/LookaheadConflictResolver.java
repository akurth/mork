package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.scanner.Scanner;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.IOException;

public class LookaheadConflictResolver implements ConflictResolver {
    private final Line[] lines;

    public LookaheadConflictResolver(Line[] lines) {
        this.lines = lines;
    }

    public int run(Scanner scanner, int mode, int eof) throws IOException {
        for (Line line : lines) {
            if (scanner.match(mode, eof, line.terminals)) {
                return line.action;
            }
        }
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
    }
}
