package compiler;

import net.sf.beezle.mork.classfile.Code;

import net.sf.beezle.mork.semantics.BuiltIn;
import net.sf.beezle.mork.semantics.IllegalLiteral;

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
