package interpreter;

public class Const extends Expression {
    private int type;
    private Object val;

    public Const(Object valInit) {
        val = valInit;
        if (val instanceof Boolean) {
            type = BOOL;
        } else if (val instanceof Integer) {
            type = INT;
        } else if (val instanceof String) {
            type = STR;
        } else {
            throw new RuntimeException("illegal constant type: "
                                       + val.getClass());
        }
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public Object eval() {
        return val;
    }
}
