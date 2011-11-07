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

package net.sf.beezle.mork.mapping;

import java.io.IOException;

import net.sf.beezle.sushi.util.IntBitSet;

import net.sf.beezle.mork.scanner.Position;

/**
 * <code>Mapper.run()</code> reports errors by taking the registered error handler and
 * invoking the respective method of this inteferace.
 */
public interface ErrorHandler {
    void lexicalError(Position pos);
    void syntaxError(Position pos, IntBitSet shiftable);
    void semanticError(Position pos, Exception e);
    void ioError(String position, String message, IOException e);
}
