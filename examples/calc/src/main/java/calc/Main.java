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

package calc;

import net.oneandone.mork.mapping.Mapper;

/**
 * Calculate simple expressions.
 * A kind of Hello-World example for tools like Mork.
 */
public class Main {
    public static void main(String[] args) {
        Mapper mapper;

        mapper = new Mapper("calc.Mapper");
        System.out.println("(press ctrl-c to quit)");
        mapper.repl("> ", null);
    }

    public static int expr(int result) {
        return result;
    }

    public static int add(int left, int right) {
        return left + right;
    }

    public static int sub(int left, int right) {
        return left - right;
    }

    public static int mult(int left, int right) {
        return left * right;
    }

    public static int div(int left, int right) throws Exception {
        if (right == 0) {
            throw new Exception("division by zero");
        }
        return left / right;
    }
}
