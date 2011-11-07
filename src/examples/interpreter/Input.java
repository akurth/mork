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

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Input extends Statement {
    private Variable var;

    public Input(Reference ref) {
        var = ref.getVar();
    }

    @Override
    public void execute() {
        try {
            BufferedReader input;
            String str;

            input = new BufferedReader(new InputStreamReader(System.in));
            str = input.readLine();
            switch (var.getType()) {
            case Expression.BOOL:
                var.set(Boolean.valueOf(str));
                break;
            case Expression.INT:
                var.set(new Integer(str));
                break;
            case Expression.STR:
                var.set(str);
                break;
            default:
                throw new RuntimeException("unknown type: " + var.getType());
            }
        } catch (Exception e) {
            System.out.println("input failed: " + e);
            System.exit(1);
        }
    }
}
