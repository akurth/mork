package interpreter;

public class Script {
    private final Declarations decls;
    private final Statement stmt;

    public Script(Declarations declsInit, Statement stmtInit) {
        decls = declsInit;
        stmt = stmtInit;
    }

    public void run() {
        System.out.println("running script");
        stmt.execute();
    }
}
