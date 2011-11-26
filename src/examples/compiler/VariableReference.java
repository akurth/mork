package compiler;

import net.sf.beezle.mork.classfile.Code;

/**
 * Local variable reference.
 */
public class VariableReference extends LValue {
    private Variable var;
    private int store;  // store opcode
    private int load; // load opcode

    public VariableReference(Declarations decls, String name) throws SemanticError {
        if (decls == null) {
            throw new IllegalArgumentException();
        }
        var = decls.lookup(name);
        if (var.getType() == Int.TYPE) {
            store = ISTORE;
            load = ILOAD;
        } else {
            store = ASTORE;
            load = ALOAD;
        }
    }

    @Override
    public Type getType() {
        return var.getType();
    }

    @Override
    public void translateAssign(Code dest) {
        dest.emit(store, var.getAddress());
    }

    @Override
    public void translate(Code dest) {
        dest.emit(load, var.getAddress());
    }
}
