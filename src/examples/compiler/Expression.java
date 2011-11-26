package compiler;

import net.sf.beezle.mork.classfile.Bytecodes;
import net.sf.beezle.mork.classfile.Code;

public abstract class Expression implements Bytecodes {
    public abstract Type getType();
    public abstract void translate(Code dest);
}
