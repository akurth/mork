package interpreter;

public class Assign extends Statement {
    private final Variable var;
    private final Expression expr;

    public Assign(Reference ref, Expression exprInit) throws SemanticError {
        var = ref.getVar();
        expr = exprInit;
        if (var.getType() != expr.getType()) {
            throw new SemanticError("type missmatch");
        }
    }

    @Override
    public void execute() {
        var.set(expr.eval());
    }
}
