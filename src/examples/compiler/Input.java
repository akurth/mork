package compiler;

import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Code;
import net.sf.beezle.mork.classfile.MethodRef;

public class Input extends Statement {
    private LValue left;
    private MethodRef inputMethod;

    private static final MethodRef INPUT_INT = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.INT, "inputInt");

    private static final MethodRef INPUT_STRING = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.STRING, "inputString");



    public Input(LValue left) throws SemanticError {
        Type type;

        this.left = left;
        type = left.getType();
        if (type == Int.TYPE) {
            inputMethod = INPUT_INT;
        } else if (type == Str.TYPE) {
            inputMethod = INPUT_STRING;
        } else {
            throw new SemanticError("cannot input this type: " + type);
        }
    }

    @Override
    public void translate(Code dest) {
        dest.emit(INVOKESTATIC, inputMethod);
        left.translateAssign(dest);
    }
}
