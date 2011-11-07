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

package command;

public class Command {
    private String name;
    private String description;
    private Declarations decls;
    private Line line;

    public Command(String name, String desciption, Declarations decls, Line line) {
        this.name = name;
        this.description = desciption;
        this.decls = decls;
        this.line = line;
    }

    public void run() {
        String cmd;
        Console console;

        if (decls.runFrontend(name, description)) {
            cmd = line.eval();
            console = new Console();
            console.execute(cmd);
        } else {
            // dialog canceled - do nothing
        }
    }
}
