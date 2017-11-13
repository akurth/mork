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
