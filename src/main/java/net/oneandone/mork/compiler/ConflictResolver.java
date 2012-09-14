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
package net.oneandone.mork.compiler;

import net.oneandone.mork.parser.Parser;
import net.oneandone.mork.parser.ParserTable;
import net.oneandone.mork.scanner.Scanner;

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
