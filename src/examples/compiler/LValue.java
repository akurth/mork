package compiler;

import net.sf.beezle.mork.classfile.Code;

public abstract class LValue extends Expression {
    public abstract void translateAssign(Code code);
}
