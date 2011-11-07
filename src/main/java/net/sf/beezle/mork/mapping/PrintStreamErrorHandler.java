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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.beezle.sushi.util.IntBitSet;

import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.scanner.Position;

/**
 * ErrorHandler that prints messages to the PrintStream specified in the constructor.
 */

public class PrintStreamErrorHandler implements ErrorHandler {
    /**
     * Where to send error messages.
     */
    private PrintStream dest;

    private List errors;

    /**
     * @param dest may be null
     */
    public PrintStreamErrorHandler(PrintStream dest) {
        this.dest = dest;
        this.errors = new ArrayList();
    }

    /**
     * This method is used by the various <code>error</code> methods to actually print
     * a message.
     *
     * @param pos      where the problem occurred
     * @param message  problem description
     */
    public void report(String pos, String message, Object error) {
        if (dest != null) {
            dest.println(pos + ": " + message);
        }
        errors.add(error);
    }

    public void lexicalError(Position pos) {
        report(pos.toString(), "illegal token", pos);
    }

    public void syntaxError(Position pos, IntBitSet shiftable) {
        report(pos.toString(), "syntax error", pos);
    }

    public void semanticError(Position pos, Exception e) {
        report(pos.toString(), e.getMessage(), pos);
    }

    public void ioError(String pos, String message, IOException e) {
        report(pos, message + ": " + e.getMessage(), message);
    }

    public void error(String pos, GenericException e) {
        report(pos, e.getMessage(), e);
    }

    public void error(String pos, String message) {
        report(pos, message, message);
    }

    public int getErrorCount() {
        return errors.size();
    }

    public Object getLastError() {
        return errors.get(errors.size() - 1);
    }
}
