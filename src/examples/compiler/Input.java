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

import de.mlhartme.mork.classfile.ClassRef;
import de.mlhartme.mork.classfile.Code;
import de.mlhartme.mork.classfile.MethodRef;

public class Input extends Statement {
    private LValue left;
    private MethodRef inputMethod;

    private static final MethodRef INPUT_INT = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.INT, "inputInt");

    private static final MethodRef INPUT_STRING = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.STRING, "inputString");



    public Input(LValue left) throws SemanticError {
        Type type;

        this.left = left;
        type = left.getType();
        if (type == Int.TYPE) {
            inputMethod = INPUT_INT;
        } else if (type == Str.TYPE) {
            inputMethod = INPUT_STRING;
        } else {
            throw new SemanticError("cannot input this type: " + type);
        }
    }

    @Override
    public void translate(Code dest) {
        dest.emit(INVOKESTATIC, inputMethod);
        left.translateAssign(dest);
    }
}
