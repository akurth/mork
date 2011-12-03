package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.scanner.Scanner;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.IOException;

public class ConflictResolver {
    private final IntBitSet terminalsA;
    private final int actionA;
    private final IntBitSet terminalsB;
    private final int actionB;

    private final IntBitSet both;

    public ConflictResolver(IntBitSet terminalsA, int actionA, IntBitSet terminalsB, int actionB) {
        this.terminalsA = terminalsA;
        this.actionA = actionA;
        this.terminalsB = terminalsB;
        this.actionB = actionB;
        this.both = new IntBitSet(terminalsA);
        this.both.addAll(terminalsB);
    }

    public int run(Scanner scanner, int mode) throws IOException {
        int terminal;

        terminal = scanner.find(mode, both);
        if (terminal >= 0) {
            if (terminalsA.contains(terminal)) {
                return actionA;
            } else if (terminalsB.contains(terminal)) {
                return actionB;
            }
        }
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
    }
}
