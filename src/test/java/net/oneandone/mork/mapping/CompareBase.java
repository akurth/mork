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
package net.oneandone.mork.mapping;

import junit.framework.TestCase;
import net.oneandone.mork.semantics.Attribute;
import net.oneandone.mork.semantics.Compare;
import net.oneandone.mork.semantics.Type;
import net.oneandone.sushi.util.IntArrayList;

import java.util.ArrayList;
import java.util.List;

public abstract class CompareBase extends TestCase implements Compare {
    protected Attribute attr;

    public CompareBase() {
        super();

        attr = new Attribute(0, "dummy", new Type(String.class));
    }

    public List<Attribute> attrs(int n) {
        List<Attribute> l;

        l = new ArrayList<Attribute>();
        for (; n > 0; n--) {
            l.add(attr);
        }
        return l;
    }

    public IntArrayList ofs(int a) {
        IntArrayList l;

        l = new IntArrayList();
        l.add(a);
        return l;
    }

    public IntArrayList ofs(int a, int b) {
        IntArrayList l;

        l = new IntArrayList();
        l.add(a);
        l.add(b);
        return l;
    }

    public IntArrayList ofs(int a, int b, int c) {
        IntArrayList l;

        l = new IntArrayList();
        l.add(a);
        l.add(b);
        l.add(c);
        return l;
    }
}
