package interpreter;

public class Script {
    private Declarations decls;
    private Statement stmt;

    public Script(Declarations declsInit, Statement stmtInit) {
        decls = declsInit;
        stmt = stmtInit;
    }

    public void run() {
        System.out.println("running script");
        stmt.execute();
    }
}
