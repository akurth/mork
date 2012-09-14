package compiler;

import net.oneandone.mork.classfile.Code;

public class Block extends Statement {
    private Statement[] body;

    public Block() {
        this(new Statement[0]);
    }

    public Block(Statement[] body) {
        this.body = body;
    }

    @Override
    public void translate(Code dest) {
        int i;

        for (i = 0; i < body.length; i++) {
            body[i].translate(dest);
        }
    }
}
