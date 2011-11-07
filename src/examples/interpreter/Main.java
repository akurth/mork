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

import net.sf.beezle.mork.mapping.Mapper;

/** Command line invokation. */

public class Main {
    public static void main(String[] args) {
        Mapper mapper;
        Object[] result;
        Script script;

        if (args.length != 1) {
            System.out.println("usage: interpreter.Main <filename>");
        } else {
            mapper = new Mapper("interpreter.Mapper");
            result = mapper.run(args[0]);
            if (result == null) {
                return;
            }
            script = (Script) result[0];
            script.run();
        }
    }
}
