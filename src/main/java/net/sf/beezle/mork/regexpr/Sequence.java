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
    public Object visit(Action action) {
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
