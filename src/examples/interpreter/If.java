package interpreter;

public class If extends Statement {
    private Expression test;
    private Statement yes;
    private Statement no;

    public If(Expression test, Statement yes, Statement no) throws SemanticError {
        this.test = test;
        this.yes = yes;
        if (no == null) {
            this.no = Block.createNop();
        } else {
            this.no = no;
        }
        if (test.getType() != Expression.BOOL) {
            throw new SemanticError("boolean expression expected");
        }
    }

    @Override
    public void execute() {
        if (test.evalBool()) {
            yes.execute();
        } else {
            no.execute();
        }
    }
}
