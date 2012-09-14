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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper to translate SemanticsBuffer into Semantics
 * TODO: merge this functionality into SemanticsBuffer?
 */
public class Layout {
    /** List of List of Attributes. Indexed by symbols. */
    private final List<List<Attribute>> attrs;

    public Layout() {
        attrs = new ArrayList<List<Attribute>>();
    }

    //--

    // returns location or -1
    public int locate(Attribute attr) {
        List lst;
        int i;
        int max;

        if (attr.symbol < attrs.size()) {
            lst = (List) attrs.get(attr.symbol);
            max = lst.size();
            for (i = 0; i < max; i++) {
                if (attr == lst.get(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int getLocationCount(int symbol) {
        if (symbol < attrs.size()) {
            return ((List) attrs.get(symbol)).size();
        } else {
            return 0;
        }
    }

    public Attribution createAttribution(AttributionBuffer ab) {
        int resultLocation;
        int[] argsLocation;
        int i;
        int[] argsOfs;
        AttributeOccurrence arg;

        resultLocation = locate(ab.result.attr);
        if (resultLocation == -1) {
            throw new RuntimeException("invalid semantics");
        }
        argsLocation = new int[ab.getArgCount()];
        argsOfs = new int[ab.getArgCount()];
        for (i = 0; i < argsLocation.length; i++) {
            arg = ab.getArg(i);
            argsLocation[i] = locate(arg.attr);
            argsOfs[i] = arg.ofs;
            if (argsLocation[i] == -1) {
                throw new RuntimeException("invalid semantics");
            }
        }
        return new Attribution(ab.function, ab.result.ofs, resultLocation, argsOfs, argsLocation);
    }

    //--

    // adds if new ...
    public void add(Attribute attr) {
        int symbol;
        List<Attribute> lst;

        if (locate(attr) == -1) {
            symbol = attr.symbol;
            while (attrs.size() <= symbol) {
                attrs.add(new ArrayList<Attribute>());
            }
            lst = attrs.get(symbol);
            lst.add(attr);
        }
    }

    public void add(AttributionBuffer ab) {
        int i;
        int max;

        add(ab.result.attr);
        max = ab.getArgCount();
        for (i = 0; i < max; i++) {
            add(ab.getArg(i).attr);
        }
    }
}
