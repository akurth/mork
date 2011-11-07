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

package interpreter;

public class While extends Statement {
    private Expression test;
    private Statement body;

    public While(Expression testInit, Statement bodyInit) throws SemanticError {
        test = testInit;
        body = bodyInit;
        if (test.getType() != Expression.BOOL) {
            throw new SemanticError("boolean expression expected");
        }
    }

    @Override
    public void execute() {
        boolean ok;

        for (ok = test.evalBool(); ok; ok = test.evalBool()) {
            body.execute();
        }
    }
}
