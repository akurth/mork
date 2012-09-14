package compiler;

import net.oneandone.mork.classfile.Bytecodes;
import net.oneandone.mork.classfile.Code;

public abstract class Expression implements Bytecodes {
    public abstract Type getType();
    public abstract void translate(Code dest);
}
