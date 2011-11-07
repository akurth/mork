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
