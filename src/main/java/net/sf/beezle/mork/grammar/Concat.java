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
package net.sf.beezle.mork.grammar;

public class Concat {
    private final int k;
    private final long firstFullValue;
    private final PrefixSet done;
    private PrefixSet todo;

    public Concat(int k) {
        this.k = k;
        this.firstFullValue = exp(Prefix.BASE, k - 1);
        this.done = new PrefixSet();
        this.todo = PrefixSet.one();
    }

    private static long exp(int x, int y) {
        long result;

        result = 1;
        for (int i = 1; i <= y; i++) {
            result *= x;
        }
        return result;
    }

    /** true when done */
    public boolean with(PrefixSet op) {
        PrefixSet next;
        long tmp;
        Prefix l;
        Prefix r;

        next = new PrefixSet();
        l = todo.iterator();
        while (l.step()) {
            r = op.iterator();
            while (r.step()) {
                tmp = Prefix.concat(l.data, r.data, k);
                if (tmp >= firstFullValue) {
                    done.add(tmp);
                } else {
                    next.add(tmp);
                }
            }
        }
        todo = next;
        return todo.isEmpty();
    }

    public PrefixSet result() {
        done.addAll(todo);
        return done;
    }
}
