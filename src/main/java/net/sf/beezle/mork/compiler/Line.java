package net.sf.beezle.mork.compiler;

import java.util.Arrays;
import java.util.List;

public class Line {
    public final int[] terminals;
    public final int action;

    public Line(int[] terminals, int action) {
        this.terminals = terminals;
        this.action = action;
    }

    public static Line lookupTerminals(List<Line> lines, int[] cmp) {
        for (Line line : lines) {
            if (Arrays.equals(line.terminals, cmp)) {
                return line;
            }
        }
        return null;
    }
}
