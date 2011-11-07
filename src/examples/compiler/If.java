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

public class If extends Statement {
    private Expression test;
    private Statement yes;
    private Statement no;

    public If(Expression test, Statement yes, Statement no) throws SemanticError {
        if (test.getType() != Int.TYPE) {
            throw new SemanticError("int expression expected");
        }
        this.test = test;
        this.yes = yes;
        if (no != null) {
            this.no = no;
        } else {
            this.no = new Block();
        }
    }

    @Override
    public void translate(Code dest) {
        int noLabel;
        int endLabel;

        noLabel = dest.declareLabel();
        endLabel = dest.declareLabel();
        test.translate(dest);
        dest.emit(IFEQ, noLabel);
        yes.translate(dest);
        dest.emit(GOTO, endLabel);
        dest.defineLabel(noLabel);
        no.translate(dest);
        dest.defineLabel(endLabel);
    }
}
