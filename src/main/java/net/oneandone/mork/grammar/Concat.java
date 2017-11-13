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
package net.oneandone.mork.grammar;

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
