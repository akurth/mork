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

/** stores the result from visiting a node */

public abstract class Action {
    public abstract Object range(char first, char last) throws ActionException;
    public abstract Object symbol(int symbol) throws ActionException;

    public abstract Object choice(Object[] body);
    public abstract Object sequence(Object[] body);
    public abstract Object loop(Object body);
    public abstract Object without(Object left, Object right) throws ActionException;
}
