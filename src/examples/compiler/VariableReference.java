/*
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package compiler;

import de.mlhartme.mork.classfile.Code;

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
