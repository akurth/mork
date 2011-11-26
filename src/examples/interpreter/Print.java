package interpreter;

public class Print extends Statement {
    private Expression expr;

    public Print(Expression exprInit) {
        expr = exprInit;
    }

    @Override
    public void execute() {
        System.out.println(expr.eval().toString());
    }
}
