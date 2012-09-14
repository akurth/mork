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



/**
 * Loop with >= 1 iterations. Caution, this is not Kleene star.
 * Using Kleene star would be problematic because translating EBNF rules
 * into grammars becomes much harder.
 */
public class Loop extends RegExpr {
    private RegExpr body;

    public Loop(RegExpr body) {
        if (body == null) {
            throw new NullPointerException();
        }

        this.body = body;
    }

    /** Kleene star. */
    public static RegExpr createStar(RegExpr bodyInit) {
        return Choice.createOption(new Loop(bodyInit));
    }

    @Override
    public Object visit(Action action) throws ActionException {
        Object tmp;

        tmp = body.visit(action);
        return action.loop(tmp);
    }

    @Override
    public String toString() {
        return "{" + body.toString() + "}";
    }
}
