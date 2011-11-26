package compiler;

public class Variable {
    private Type type;
    private String name;
    private int address;

    public Variable(Type type, String name) {
        this.type = type;
        this.name = name;
        address = -1;
    }

    public int allocate(int no) {
        address = no;
        return no + 1;
    }

    public int getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
