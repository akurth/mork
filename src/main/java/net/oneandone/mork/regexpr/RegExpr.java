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
package net.oneandone.mork.regexpr;

import java.io.Serializable;

/**
 * Regular Expression. All derived classes shall be immutable, it has
 * to be safe to share instances. Anything that can read from a buffer
 * and that can be visited is considered a regular expression.
 */

public abstract class RegExpr implements Serializable {
    /**
     * Visit this expressions and its sub-expression and perform
     * some action.
     */
    public abstract Object visit(Action action) throws ActionException;
}
