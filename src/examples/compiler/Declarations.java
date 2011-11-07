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

public class Declarations {
    private Declarations parent;
    private Variable[] vars;

    public Declarations(Declarations parent, Variable[] vars) throws SemanticError {
        this.parent = parent;
        this.vars = vars;
        checkDuplicates();
    }

    private void checkDuplicates() throws SemanticError {
        int i;
        String n;
        Variable v;

        for (i = 0; i < vars.length; i++) {
            v = vars[i];
            n = v.getName();
            if (lookup(n) != v) {
                throw new SemanticError("duplicate variable: " + n);
            }
        }
    }

    public Variable lookup(String name) throws SemanticError {
        int i;

        for (i = 0; i < vars.length; i++) {
            if (vars[i].getName().equals(name)) {
                return vars[i];
            }
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        throw new SemanticError("no such variable: " + name);
    }

    public int allocate(int address) {
        int i;

        for (i = 0; i < vars.length; i++) {
            address = vars[i].allocate(address);
        }
        return address;
    }
}
