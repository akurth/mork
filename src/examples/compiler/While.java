package compiler;

import net.sf.beezle.mork.classfile.Code;

public class While extends Statement {
    private Expression test;
    private Statement body;

    public While(Expression test, Statement body) throws SemanticError {
        this.test = test;
        this.body = body;
        if (test.getType() != Int.TYPE) {
            throw new SemanticError("boolean expression expected");
        }
    }

    @Override
    public void translate(Code dest) {
        int startLabel;
        int testLabel;

        startLabel = dest.declareLabel();
        testLabel = dest.declareLabel();
        dest.emit(GOTO, testLabel);
        dest.defineLabel(startLabel);
        body.translate(dest);

        dest.defineLabel(testLabel);
        test.translate(dest);
        dest.emit(IFNE, startLabel);
    }
}
