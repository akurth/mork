/*
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

package net.sf.beezle.mork.regexpr;

import java.util.ArrayList;
import java.util.List;

/** Left | Right, a little bit more general. */

public class Choice extends RegExpr {
    private final RegExpr[] body;

    //--

    public Choice(RegExpr ... body) {
        this.body = body;
    }

    public static RegExpr createRightOptional(RegExpr left, RegExpr right) {
        if (right == null) {
            return left;
        } else {
            return new Choice(left, right);
        }
    }

    public static RegExpr createLeftOptional(RegExpr left, RegExpr right) {
        if (left == null) {
            return right;
        } else {
            return new Choice(left, right);
        }
    }


    public static Choice createOption(RegExpr e) {
        return new Choice(e, new Sequence());
    }

    public static Choice createNot(RegExpr expr) {
        List<RegExpr> ranges, result;
        int i, max;
        RegExpr[] args;

        ranges = new ArrayList<RegExpr>();
        if (!getRanges(expr, ranges)) {
            throw new IllegalArgumentException();
        }
        result = new ArrayList<RegExpr>();
        result.add(Range.ALL);
        max = ranges.size();
        for (i = 0; i < max; i++) {
            Range.remove(result, (Range) ranges.get(i));
        }
        args = new RegExpr[result.size()];
        result.toArray(args);
        return new Choice(args);
    }

    public static boolean getRanges(RegExpr expr, List<RegExpr> result) {
        int i;
        Choice alt;

        if (expr instanceof Range) {
            result.add(expr);
            return true;
        } else if (expr instanceof Choice) {
            alt = (Choice) expr;
            for (i = 0; i < alt.body.length; i++) {
                if (!getRanges(alt.body[i], result)) {
                    return false;
                }
            }
            return true;
        } else if (expr instanceof Sequence) {
            return ((Sequence) expr).getRanges(result);
        } else {
            return false;
        }
    }

    @Override
    public Object visit(Action action) throws ActionException {
        Object[] tmps;
        int i;

        tmps = new Object[body.length];
        for (i = 0; i < tmps.length; i++) {
            tmps[i] = body[i].visit(action);
        }
        return action.choice(tmps);
    }

    @Override
    public String toString() {
        StringBuilder buf;
        int i;

        buf = new StringBuilder();
        buf.append('(');
        for (i = 0; i < body.length; i++) {
            buf.append('|');
            buf.append(body[i].toString());
        }
        buf.append("|)");
        return buf.toString();
    }
}
