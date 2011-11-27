package compiler;

import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Code;
import net.sf.beezle.mork.classfile.MethodRef;

public class Print extends Statement {
    private Expression expr;
    private MethodRef printMethod;

    private static final MethodRef PRINT_INT = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.VOID, "printInt", ClassRef.INT);

    private static final MethodRef PRINT_STRING = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.VOID, "printString", ClassRef.STRING);

    public Print(Expression expr) throws SemanticError {
        Type type;

        this.expr = expr;
        type = expr.getType();
        if (type == Int.TYPE) {
            printMethod = PRINT_INT;
        } else if (type == Str.TYPE) {
            printMethod = PRINT_STRING;
        } else {
            throw new SemanticError("cannot print this type: " + type);
        }
    }

    @Override
    public void translate(Code dest) {
        expr.translate(dest);
        dest.emit(INVOKESTATIC, printMethod);
    }
}
