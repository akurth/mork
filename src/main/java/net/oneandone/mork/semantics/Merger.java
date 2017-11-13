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
package net.oneandone.mork.semantics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Stupid data container for CopyBuffer */
public class Merger {
    public final List<State> source;

    /** attribute resulting from the merger */
    public final Attribute dest;

    // to have unique names
    private static int count = 0;

    public Merger(int destSymbol, Type destType) {
        source = new ArrayList<State>();
        dest = new Attribute(destSymbol, "merged" + count, destType);
        count++;
    }

    public static Merger forSymbol(List<Merger> mergers, int symbol) {
        int i;
        int max;
        Merger merger;

        max = mergers.size();
        for (i = 0; i < max; i++) {
            merger = (Merger) mergers.get(i);
            if (merger.dest.symbol == symbol) {
                return merger;
            }
        }
        return null;
    }

    public static Attribute map(Map<Attribute, Merger> mapping, Attribute attr) {
        Merger merger;

        merger = mapping.get(attr);
        if (merger != null) {
            return merger.dest;
        } else {
            return attr;
        }
    }
}
