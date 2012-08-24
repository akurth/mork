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
package net.sf.beezle.mork.mapping;

import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.IOException;

public class ExceptionErrorHandler implements ErrorHandler {
    public ExceptionErrorHandler() {
    }

    public void lexicalError(Position pos) throws IOException {
        report(pos.toString(), "illegal token");
    }

    public void syntaxError(Position pos, IntBitSet shiftable) throws IOException {
        report(pos.toString(), "syntax error");
    }

    public void semanticError(Position pos, Exception e) throws IOException {
        report(pos.toString(), e.getMessage());
    }

    public void close() throws IOException {
        // no deferred exceptions
    }

    //--

    protected void report(String pos, String message) throws IOException {
        throw new IOException(pos + ": " + message);
    }
}
