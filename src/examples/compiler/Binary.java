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

import net.sf.beezle.mork.classfile.Code;

public class Binary extends Expression {
    private Type type;
    private int op;
    private Expression left;
    private Expression right;

    public static Expression createRightOptional(Expression left, int op, Expression right) throws SemanticError {
        if (right == null) {
            return left;
        } else {
            return new Binary(left, op, right);
        }
    }

    public static Expression createLeftOptional(Expression left, int op, Expression right) throws SemanticError {
        if (left == null) {
            return right;
        } else {
            return new Binary(left, op, right);
        }
    }

    private Binary(Expression left, int op, Expression right) throws SemanticError {
        this.op = op;
        this.left = left;
        this.right = right;
        type = left.getType().getBinaryType(op, right.getType());
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void translate(Code code) {
        left.translate(code);
        right.translate(code);
        left.getType().translateBinary(op, code);
    }
}
