package compiler;

import net.oneandone.mork.classfile.Code;

public class Binary extends Expression {
    private Type type;
    private int op;
    private Expression left;
    private Expression right;

    public static Expression createRightOptional(Expression left, int op, Expression right) throws SemanticError {
        if (right == null) {
            return left;
        } else {
            return new Binary(left, op, right);
        }
    }

    public static Expression createLeftOptional(Expression left, int op, Expression right) throws SemanticError {
        if (left == null) {
            return right;
        } else {
            return new Binary(left, op, right);
        }
    }

    private Binary(Expression left, int op, Expression right) throws SemanticError {
        this.op = op;
        this.left = left;
        this.right = right;
        type = left.getType().getBinaryType(op, right.getType());
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void translate(Code code) {
        left.translate(code);
        right.translate(code);
        left.getType().translateBinary(op, code);
    }
}
