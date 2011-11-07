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

import de.mlhartme.mork.classfile.Bytecodes;
import de.mlhartme.mork.classfile.Code;

public abstract class Type implements Bytecodes {
    private String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean isAssignableFrom(Type type);

    public boolean isEquiv(Type type) {
        return isAssignableFrom(type) && type.isAssignableFrom(this);
    }

    public abstract Type getUnaryType(int op) throws SemanticError;
    public abstract Type getBinaryType(int op, Type second) throws SemanticError;
    public abstract void translateBinary(int op, Code dest);
    public abstract void translateUnary(int op, Code dest);
}
