package interpreter;

public class Block extends Statement {
    private final Statement[] stmts;

    public Block(Statement[] stmts) {
        this.stmts = stmts;
    }

    public static Block createNop() {
        return new Block(new Statement[0]);
    }

    @Override
    public void execute() {
        int i;

        for (i = 0; i < stmts.length; i++) {
            stmts[i].execute();
        }
    }
}
