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

package de.mlhartme.mork.mapping;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import net.sf.beezle.sushi.util.IntArrayList;

import de.mlhartme.mork.semantics.Attribute;
import de.mlhartme.mork.semantics.Compare;
import de.mlhartme.mork.semantics.Type;

public abstract class CompareBase extends TestCase implements Compare {
    protected Attribute attr;

    public CompareBase() {
        super();

        attr = new Attribute(0, "dummy", new Type(String.class));
    }

    public List attrs(int n) {
        List l;

        l = new ArrayList();
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
