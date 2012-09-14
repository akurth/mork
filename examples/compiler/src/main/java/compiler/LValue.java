package compiler;

import net.oneandone.mork.classfile.Code;

public abstract class LValue extends Expression {
    public abstract void translateAssign(Code code);
}
