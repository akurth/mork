/*
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
