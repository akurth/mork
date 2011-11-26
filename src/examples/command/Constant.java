package command;

public class Constant extends Expression {
    private String str;

    public Constant(String str) {
        this.str = str;
    }

    @Override
    public String eval() {
        return str;
    }
}
