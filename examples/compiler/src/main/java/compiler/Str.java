package compiler;

import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.classfile.MethodRef;

public class Str extends Type {
    public static final Str TYPE = new Str();

    private static final MethodRef ADD_METH = MethodRef.meth(
        ClassRef.STRING, ClassRef.STRING, "concat", ClassRef.STRING);

    private static final MethodRef EQ_METH = MethodRef.meth(
        ClassRef.STRING, ClassRef.BOOLEAN, "equals", ClassRef.STRING);

    private Str() {
        super("string");
    }

    @Override
    public boolean isAssignableFrom(Type from) {
        return from == this;
    }

    @Override
    public Type getUnaryType(int op) throws SemanticError {
        throw new SemanticError("no such operator for type string");
    }

    @Override
    public Type getBinaryType(int op, Type second) throws SemanticError {
        switch (op) {
        case Operator.EQ:
        case Operator.NE:
            if (this != second) {
                throw new SemanticError("type mismatch");
            }
            return Int.TYPE;
        case Operator.ADD:
            if (this != second) {
                throw new SemanticError("type mismatch");
            }
            return this;
        default:
            throw new SemanticError("no such operator for type string");
        }
    }

    @Override
    public void translateBinary(int op, Code dest) {
        switch (op) {
        case Operator.ADD:
            dest.emit(INVOKEVIRTUAL, ADD_METH);
            break;
        case Operator.EQ:
            dest.emit(INVOKEVIRTUAL, EQ_METH);
            break;
        case Operator.NE:
            dest.emit(INVOKEVIRTUAL, EQ_METH);
            dest.emit(LDC, 1);
            dest.emit(IXOR);
            break;
        default:
            throw new IllegalArgumentException("" + op);
        }
    }

    @Override
    public void translateUnary(int op, Code dest) {
        throw new UnsupportedOperationException();
    }
}
