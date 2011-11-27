package interpreter;

public class Variable {
    private String name;
    private int type;
    private Object val;

    public Variable(int typeInit, String nameInit) {
        name = nameInit;
        type = typeInit;
        switch (type) {  // initialize
        case Expression.BOOL:
            val = new Boolean(false);
            break;
        case Expression.INT:
            val = new Integer(0);
            break;
        case Expression.STR:
            val = new String("");
            break;
        default:
            throw new RuntimeException("unknown type: " + type);
        }
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void set(Object obj) {
        val = obj;
    }

    public Object get() {
        return val;
    }
}
