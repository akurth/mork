package interpreter;

public class While extends Statement {
    private final Expression test;
    private final Statement body;

    public While(Expression testInit, Statement bodyInit) throws SemanticError {
        test = testInit;
        body = bodyInit;
        if (test.getType() != Expression.BOOL) {
            throw new SemanticError("boolean expression expected");
        }
    }

    @Override
    public void execute() {
        boolean ok;

        for (ok = test.evalBool(); ok; ok = test.evalBool()) {
            body.execute();
        }
    }
}
