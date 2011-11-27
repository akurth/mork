package command;

/** Variable reference. */

public class Reference extends Expression {
    /** variable referenced by this expression. */
    private Variable var;

    public Reference(Declarations decls, String identifier) throws Failure {
        var = decls.lookup(identifier);
        if (var == null) {
            throw new Failure("unknown identifier: " + identifier);
        }
    }

    @Override
    public String eval() {
        return var.get();
    }
}
