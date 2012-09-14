/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.mapping;

import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.scanner.Position;
import net.oneandone.sushi.util.IntBitSet;

import java.io.IOException;
import java.io.PrintStream;

/**
 * ErrorHandler that prints messages to the PrintStream specified in the constructor.
 */
public class PrintStreamErrorHandler implements ErrorHandler {
    /**
     * Where to send error messages.
     */
    private final PrintStream dest;

    private boolean failed;

    public PrintStreamErrorHandler(PrintStream dest) {
        if (dest == null) {
            throw new IllegalArgumentException();
        }
        this.dest = dest;
        this.failed = false;
    }

    protected void report(String pos, String message) {
        dest.println(pos + ": " + message);
        failed = true;
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

    public void close() throws IOException {
        if (failed) {
            throw new IOException("mapping failed");
        }
    }
}
