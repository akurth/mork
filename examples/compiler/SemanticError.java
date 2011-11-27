package compiler;

/**
 * Checked exception do indicate semantic errors (e.g. "undefined identifiered").
 */
public class SemanticError extends Exception {
    public SemanticError(String msg) {
        super(msg);
    }
}
