package net.sf.beezle.mork.compiler;

public class Resolution {
    private final ResolutionLine left;
    private final ResolutionLine right;

    public Resolution(ResolutionLine left, ResolutionLine right) {
        this.left = left;
        this.right = right;
    }
}
