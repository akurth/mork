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

import net.oneandone.mork.scanner.Position;
import net.oneandone.sushi.util.IntBitSet;

import java.io.IOException;

/**
 * <code>Mapper.run()</code> reports errors by taking the registered error handler and
 * invoking the respective method of this inteferace.
 *
 * Implementations may choose between 1) report exceptions immediatly by throwing an IOException in the error methods,
 * or 2) collecting errors and throwing an exception in the close method.
 */
public interface ErrorHandler {
    void lexicalError(Position pos) throws IOException;
    void syntaxError(Position pos, IntBitSet shiftable) throws IOException;
    void semanticError(Position pos, Exception e) throws IOException;

    /** Throws an exception when one of the above methods was called. */
    void close() throws IOException;
}
