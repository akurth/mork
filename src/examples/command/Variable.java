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

public class Variable {
    /** used to refer to the variable */
    private String name;

    /** used in the frontend to label the variable's input field. */
    private String label;

    /** contents */
    private String value;

    public Variable(String name, String label) {
        this.name = name;
        this.label = label;
        this.value = "";
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String get() {
        return value;
    }

    public void set(String obj) {
        value = obj;
    }
}
