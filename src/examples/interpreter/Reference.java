package interpreter;

public class Reference extends Expression {
    private Variable var;

    public Reference(Declarations ctx, String name) throws SemanticError {
        var = ctx.find(name);
        if (var == null) {
            throw new SemanticError("unkown variable: " + name);
        }
    }

    @Override
    public int getType() {
        return var.getType();
    }

    @Override
    public Object eval() {
        return var.get();
    }

    public Variable getVar() {
        return var;
    }
}
