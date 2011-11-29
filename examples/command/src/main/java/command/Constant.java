package command;

public class Constant extends Expression {
    private final String str;

    public Constant(String str) {
        this.str = str;
    }

    @Override
    public String eval() {
        return str;
    }
}
