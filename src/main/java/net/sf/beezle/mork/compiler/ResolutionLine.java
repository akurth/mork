package net.sf.beezle.mork.compiler;

import net.sf.beezle.sushi.util.IntBitSet;

public class ResolutionLine {
    public final int symbol;
    public final IntBitSet terminals;

    public ResolutionLine(int symbol, IntBitSet terminals) {
        this.symbol = symbol;
        this.terminals = terminals;
    }
}
