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
package net.oneandone.mork.regexpr;

import net.oneandone.mork.misc.StringArrayList;

/**
 * Symbol constant for a regular expression.
 */

public class Symbol extends RegExpr {
    private final int symbol;

    public Symbol(int symbolInit) {
        symbol = symbolInit;
    }

    @Override
    public Object visit(Action action) throws ActionException {
        return action.symbol(symbol);
    }

    @Override
    public String toString() {
        return "" + symbol;
    }

    public String toString(StringArrayList symbolTable) {
        return symbolTable.get(symbol);
    }
}
