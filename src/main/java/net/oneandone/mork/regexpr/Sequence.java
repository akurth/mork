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

import java.util.List;

public class Sequence extends RegExpr {
    private RegExpr[] body;

    public Sequence() {  // Epsilon
        body = new RegExpr[0];
    }

    public Sequence(RegExpr[] bodyInit) {
        body = bodyInit;
    }

    public Sequence(RegExpr left, RegExpr right) {
        if ((left == null) || (right == null)) {
            throw new NullPointerException();
        }

        body = new RegExpr[2];
        body[0] = left;
        body[1] = right;
    }

    // returns a Range for strings of length 1
    public static RegExpr createKeyword(String str) {
        int i;
        RegExpr[] chars;

        if (str.length() == 1) {
            return new Range(str.charAt(0));
        } else {
            chars = new RegExpr[str.length()];
            for (i = 0; i < chars.length; i++) {
                chars[i] = new Range(str.charAt(i));
            }
            return new Sequence(chars);
        }
    }

    public static Sequence createTimes(RegExpr body, int count) {
        RegExpr[] seq;
        int i;

        seq = new RegExpr[count];
        for (i = 0; i < count; i++) {
            seq[i] = body;
        }
        return new Sequence(seq);
    }

    public boolean getRanges(List<RegExpr> result) {
        if (body.length != 1) {
            return false;
        }
        return Choice.getRanges(body[0], result);
    }

    @Override
    public Object visit(Action action) throws ActionException {
        Object[] tmps;
        int i;

        tmps = new Object[body.length];
        for (i = 0; i < tmps.length; i++) {
            tmps[i] = body[i].visit(action);
        }
        return action.sequence(tmps);
    }

    @Override
    public String toString() {
        StringBuilder buf;
        int i;

        buf = new StringBuilder();
        buf.append('(');
        for (i = 0; i < body.length; i++) {
            buf.append(' ');
            buf.append(body[i].toString());
        }
        buf.append(" )");
        return buf.toString();
    }
}
