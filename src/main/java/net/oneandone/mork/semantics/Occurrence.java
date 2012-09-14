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
package net.oneandone.mork.semantics;

import java.util.List;

public class Occurrence {
    public static final int UNBOUNDED = Integer.MAX_VALUE;

    public static final Occurrence ONE = new Occurrence(1, 1);

    public final int min;
    public final int max;

    public Occurrence(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int card() {
        if (max > 1) {
            return Type.SEQUENCE;
        }
        if (min == 0) {
            return Type.OPTION;
        }
        return Type.VALUE;
    }

    public static Occurrence sequence(List occs) {
        int i;
        int size;
        Occurrence occ;
        int min;
        int max;
        int recursion;

        size = occs.size();
        min = 0;
        max = 0;
        recursion = 0;
        for (i = 0; i < size; i++) {
            occ = (Occurrence) occs.get(i);
            if (occ == null) {
                recursion++;
            } else {
                min += occ.min;
                if (occ.max == UNBOUNDED) {
                    max = UNBOUNDED;
                } else {
                    max += occ.max;
                }
            }
        }
        if (recursion > 0) {
            if (recursion == size) {
                return null;
            } else {
                return new Occurrence(min, UNBOUNDED);
            }
        } else {
            return new Occurrence(min, max);
        }
    }

    public static Occurrence alternate(List<Occurrence> occs) {
        int i;
        int size;
        Occurrence occ;
        int min;
        int max;
        boolean defined;

        size = occs.size();
        if (size == 0) {
            throw new IllegalArgumentException();
        }
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        defined = false;
        for (i = 0; i < size; i++) {
            occ = (Occurrence) occs.get(i);
            if (occ != null) {
                min = Math.min(occ.min, min);
                max = Math.max(occ.max, max);
                defined = true;
            }
        }
        if (defined) {
            return new Occurrence(min, max);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return min + ":" + max;
    }
}
