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

import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.scanner.Scanner;

import java.io.IOException;

public class ConflictResolver {
    public final Line[] lines;

    public ConflictResolver(Line[] lines) {
        this.lines = lines;
    }

    public int run(Scanner scanner, int mode, int eof) throws IOException {
        for (Line line : lines) {
            if (scanner.match(mode, eof, line.terminals)) {
                return line.action;
            }
        }
        return ParserTable.createValue(Parser.SPECIAL, Parser.SPECIAL_ERROR);
    }
}
