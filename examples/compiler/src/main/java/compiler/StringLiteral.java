package compiler;

import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.semantics.BuiltIn;
import net.oneandone.mork.semantics.IllegalLiteral;

public class StringLiteral extends Expression {
    private String str;

    public StringLiteral(String str) throws IllegalLiteral {
        this.str = BuiltIn.parseString(str);
    }

    @Override
    public Type getType() {
        return Str.TYPE;
    }

    @Override
    public void translate(Code dest) {
        dest.emit(LDC, str);
    }
}
