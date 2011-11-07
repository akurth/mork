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

public class While extends Statement {
    private Expression test;
    private Statement body;

    public While(Expression test, Statement body) throws SemanticError {
        this.test = test;
        this.body = body;
        if (test.getType() != Int.TYPE) {
            throw new SemanticError("boolean expression expected");
        }
    }

    @Override
    public void translate(Code dest) {
        int startLabel;
        int testLabel;

        startLabel = dest.declareLabel();
        testLabel = dest.declareLabel();
        dest.emit(GOTO, testLabel);
        dest.defineLabel(startLabel);
        body.translate(dest);

        dest.defineLabel(testLabel);
        test.translate(dest);
        dest.emit(IFNE, startLabel);
    }
}
