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

public class Program implements Bytecodes {
    private Block body;
    private Declarations[] scopes;

    public Program(Declarations[] scopes, Block body) {
        this.scopes = scopes;
        this.body = body;
    }

    public Code translate() {
        Code result;
        int i;

        result = new Code();
        result.locals = 1; // 0 is reserved for this
        for (i = 0; i < scopes.length; i++) {
            result.locals = scopes[i].allocate(result.locals);
        }
        body.translate(result);
        result.emit(RETURN);
        return result;
    }
}
