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

import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.scanner.Position;
import net.sf.beezle.sushi.util.IntBitSet;

import java.io.IOException;
import java.io.PrintStream;

/**
 * ErrorHandler that prints messages to the PrintStream specified in the constructor.
 */
public class PrintStreamErrorHandler implements ErrorHandler {
    public static final PrintStreamErrorHandler STDERR = new PrintStreamErrorHandler(System.err);

    /**
     * Where to send error messages.
     */
    private final PrintStream dest;

    public PrintStreamErrorHandler(PrintStream dest) {
        if (dest == null) {
            throw new IllegalArgumentException();
        }
        this.dest = dest;
    }

    protected void report(String pos, String message) {
        dest.println(pos + ": " + message);
    }

    public void lexicalError(Position pos) {
        report(pos.toString(), "illegal token");
    }

    public void syntaxError(Position pos, IntBitSet shiftable) {
        report(pos.toString(), "syntax error");
    }

    public void semanticError(Position pos, Exception e) {
        report(pos.toString(), e.getMessage());
    }

    public void error(String pos, GenericException e) {
        report(pos, e.getMessage());
    }

    public void error(String pos, String message) {
        report(pos, message);
    }
}
