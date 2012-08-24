/**
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
package net.sf.beezle.mork.compiler;

import java.util.Arrays;
import java.util.List;

public class Line {
    /** sequence of terminals - not alternatives */
    public final int[] terminals;
    public final int action;

    public Line(int[] terminals, int action) {
        this.terminals = terminals;
        this.action = action;
    }

    public static Line lookupTerminals(List<Line> lines, int[] cmp) {
        for (Line line : lines) {
            if (Arrays.equals(line.terminals, cmp)) {
                return line;
            }
        }
        return null;
    }
}
