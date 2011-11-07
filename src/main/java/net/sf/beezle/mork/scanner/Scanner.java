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

package net.sf.beezle.mork.scanner;

import java.io.IOException;

/**
 * A token stream, input for parsers.
 * regular expressions.
 */

public interface Scanner {
    /** scans the next terminal and returns it. */
    int eat(int mode) throws IOException;

    /** assigns the position of the last terminal returned by eat. */
    void getPosition(Position result);

    /** returns the text of the last terminal returned by eat. */
    String getText();
}
