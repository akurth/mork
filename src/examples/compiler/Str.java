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

public class Str extends Type {
    public static final Str TYPE = new Str();

    private static final MethodRef ADD_METH = MethodRef.meth(
        ClassRef.STRING, ClassRef.STRING, "concat", ClassRef.STRING);

    private static final MethodRef EQ_METH = MethodRef.meth(
        ClassRef.STRING, ClassRef.BOOLEAN, "equals", ClassRef.STRING);

    private Str() {
        super("string");
    }

    @Override
    public boolean isAssignableFrom(Type from) {
        return from == this;
    }

    @Override
    public Type getUnaryType(int op) throws SemanticError {
        throw new SemanticError("no such operator for type string");
    }

    @Override
    public Type getBinaryType(int op, Type second) throws SemanticError {
        switch (op) {
        case Operator.EQ:
        case Operator.NE:
            if (this != second) {
                throw new SemanticError("type mismatch");
            }
            return Int.TYPE;
        case Operator.ADD:
            if (this != second) {
                throw new SemanticError("type mismatch");
            }
            return this;
        default:
            throw new SemanticError("no such operator for type string");
        }
    }

    @Override
    public void translateBinary(int op, Code dest) {
        switch (op) {
        case Operator.ADD:
            dest.emit(INVOKEVIRTUAL, ADD_METH);
            break;
        case Operator.EQ:
            dest.emit(INVOKEVIRTUAL, EQ_METH);
            break;
        case Operator.NE:
            dest.emit(INVOKEVIRTUAL, EQ_METH);
            dest.emit(LDC, 1);
            dest.emit(IXOR);
            break;
        default:
            throw new IllegalArgumentException("" + op);
        }
    }

    @Override
    public void translateUnary(int op, Code dest) {
        throw new UnsupportedOperationException();
    }
}
