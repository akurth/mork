package compiler;

import net.oneandone.mork.classfile.Code;

public class If extends Statement {
    private Expression test;
    private Statement yes;
    private Statement no;

    public If(Expression test, Statement yes, Statement no) throws SemanticError {
        if (test.getType() != Int.TYPE) {
            throw new SemanticError("int expression expected");
        }
        this.test = test;
        this.yes = yes;
        if (no != null) {
            this.no = no;
        } else {
            this.no = new Block();
        }
    }

    @Override
    public void translate(Code dest) {
        int noLabel;
        int endLabel;

        noLabel = dest.declareLabel();
        endLabel = dest.declareLabel();
        test.translate(dest);
        dest.emit(IFEQ, noLabel);
        yes.translate(dest);
        dest.emit(GOTO, endLabel);
        dest.defineLabel(noLabel);
        no.translate(dest);
        dest.defineLabel(endLabel);
    }
}
