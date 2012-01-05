package net.sf.beezle.mork.compiler;

public class Line {
    public final int[] terminals;
    public final int action;

    public Line(int[] terminals, int action) {
        this.terminals = terminals;
        this.action = action;
    }
}
