package compiler;

import net.sf.beezle.mork.classfile.Code;

public class Unary extends Expression {
    private Type type;
    private int op;
    private Expression body;

    public Unary(int op, Expression body) throws SemanticError {
        this.op = op;
        this.body = body;
        this.type = body.getType().getUnaryType(op);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void translate(Code code) {
        body.translate(code);
        body.getType().translateUnary(op, code);
    }
}
