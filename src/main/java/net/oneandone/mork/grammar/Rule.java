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
package net.oneandone.mork.grammar;

import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.regexpr.RegExpr;

import java.io.Serializable;

/**
 * Defines a symbol by a redular expression.
 */

public class Rule implements Serializable {
    private int left;
    private RegExpr right;

    public Rule(int leftInit, RegExpr rightInit) {
        if (rightInit == null) {
            throw new NullPointerException();
        }
        left = leftInit;
        right = rightInit;
    }

    public int getLeft() {
        return left;
    }

    public RegExpr getRight() {
        return right;
    }

    public String toString(StringArrayList symTab) {
        return symTab.get(left) + "\t::= " + right.toString() + ";";
    }
}
