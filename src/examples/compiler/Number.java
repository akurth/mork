package compiler;

import net.sf.beezle.mork.classfile.Code;

public class Number extends Expression {
    private int num;

    public Number(String str) {
        // throws an unchecked exception if str is not a number.
        // That's fine since this would indicate a bug: the grammar has to ensure a number
        num = Integer.parseInt(str);
    }

    public Number(int num) {
        this.num = num;
    }

    @Override
    public Type getType() {
        return Int.TYPE;
    }

    @Override
    public String toString() {
        return "" + num;
    }

    @Override
    public void translate(Code code) {
        code.emit(LDC, num);
    }
}
