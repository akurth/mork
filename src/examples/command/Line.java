package command;

/**
 * A command line is a sequence of expression.
 */

public class Line {
    /**
     * Concatenating these expressions forms the command line
     * to be executed.
     */
    private Expression[] expressions;

    public Line(Expression[] expressions) {
        this.expressions = expressions;
    }

    public String eval() {
        StringBuffer buffer;
        int i;

        buffer = new StringBuffer();
        for (i = 0; i < expressions.length; i++) {
            buffer.append(expressions[i].eval());
        }
        return buffer.toString();
    }
}
