package compiler;

import net.sf.beezle.mork.classfile.Bytecodes;
import net.sf.beezle.mork.classfile.Code;

public abstract class Statement implements Bytecodes {
    public abstract void translate(Code dest);
}
