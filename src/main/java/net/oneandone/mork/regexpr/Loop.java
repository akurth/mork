/**
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
