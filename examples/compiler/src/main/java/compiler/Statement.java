package compiler;

import net.oneandone.mork.classfile.Bytecodes;
import net.oneandone.mork.classfile.Code;

public abstract class Statement implements Bytecodes {
    public abstract void translate(Code dest);
}
