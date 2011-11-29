package compiler;

import net.sf.beezle.mork.classfile.Bytecodes;
import net.sf.beezle.mork.classfile.Code;

public abstract class Type implements Bytecodes {
    private final String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean isAssignableFrom(Type type);

    public boolean isEquiv(Type type) {
        return isAssignableFrom(type) && type.isAssignableFrom(this);
    }

    public abstract Type getUnaryType(int op) throws SemanticError;
    public abstract Type getBinaryType(int op, Type second) throws SemanticError;
    public abstract void translateBinary(int op, Code dest);
    public abstract void translateUnary(int op, Code dest);
}
