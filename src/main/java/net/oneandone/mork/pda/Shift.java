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
package net.oneandone.mork.pda;

import net.oneandone.mork.misc.StringArrayList;

public class Shift {
    /** symbol or eof */
    public final int symbol;
    public final int end;

    public Shift(int symbol, int end) {
        this.symbol = symbol;
        this.end = end;
    }

    public String toString(StringArrayList symbolTable) {
        StringBuilder result;

        result = new StringBuilder();
        result.append("shift ");
        result.append(symbolTable.getOrIndex(symbol));
        result.append(" -> " + end + '\n');
        return result.toString();
    }
}
