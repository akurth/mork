package interpreter;

public class Declarations {
    private Variable[] vars;

    public Declarations(Variable[] varsInit) {
        vars = varsInit;
    }

    public Variable find(String name) {
        int i;

        for (i = 0; i < vars.length; i++) {
            if (name.equals(vars[i].getName())) {
                return vars[i];
            }
        }
        return null;
    }
}
