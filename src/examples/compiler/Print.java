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

import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Code;
import net.sf.beezle.mork.classfile.MethodRef;

public class Print extends Statement {
    private Expression expr;
    private MethodRef printMethod;

    private static final MethodRef PRINT_INT = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.VOID, "printInt", ClassRef.INT);

    private static final MethodRef PRINT_STRING = MethodRef.meth(
        new ClassRef(Runtime.class), ClassRef.VOID, "printString", ClassRef.STRING);

    public Print(Expression expr) throws SemanticError {
        Type type;

        this.expr = expr;
        type = expr.getType();
        if (type == Int.TYPE) {
            printMethod = PRINT_INT;
        } else if (type == Str.TYPE) {
            printMethod = PRINT_STRING;
        } else {
            throw new SemanticError("cannot print this type: " + type);
        }
    }

    @Override
    public void translate(Code dest) {
        expr.translate(dest);
        dest.emit(INVOKESTATIC, printMethod);
    }
}
