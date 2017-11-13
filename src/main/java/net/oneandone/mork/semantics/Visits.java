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

import net.oneandone.mork.misc.GenericException;
import net.oneandone.graph.CyclicDependency;
import net.oneandone.graph.EdgeIterator;
import net.oneandone.graph.Graph;
import net.oneandone.sushi.util.Util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Build visit sequence for ordered attribute grammar. Based on the paper
 * Uwe Kastens: "Ordered Attribute Grammars", Acta Informatics, 1980.
 */
public class Visits {
    /** Attributions or Integer objects. */
    private final Object[] visits;

    public static Visits forEDP(int prod, Graph<AttributeOccurrence> edp, Ag sems, List<Attribute>[][] as, Layout layout) throws GenericException {
        EdgeIterator<AttributeOccurrence> iter;
        AttributeOccurrence left;
        AttributeOccurrence right;
        int i;
        int max;
        Graph<Object> visitRelation;  // Attribute, AttributionBuffer, or Integer
        Set<AttributionBuffer> all;
        Object leftMapped;
        Object rightMapped;
        Object obj;
        List<Object> lst;

        visitRelation = new Graph<Object>();

        // attributions first - to enforce textual order
        all = new LinkedHashSet<AttributionBuffer>();
        sems.getProduction(prod, all);
        for (Object x : all) {
        	visitRelation.addNode(x);
        }
        iter = edp.edges();
        while (iter.step()) {
            left = iter.left();
            right = iter.right();
            leftMapped = map(prod, left, sems, as);
            rightMapped = map(prod, right, sems, as);
            visitRelation.addEdge(leftMapped, rightMapped);
        }
        visitRelation.removeDirectCycles();
        try {
			lst = visitRelation.sort();
		} catch (CyclicDependency e) {
            throw new GenericException("cyclic dependency in prod " + prod);
		}
        if (layout != null) {
            max = lst.size();
            for (i = 0; i < max; i++) {
                obj = lst.get(i);
                if (obj instanceof AttributionBuffer) {
                    lst.set(i, layout.createAttribution((AttributionBuffer) obj));
                }
            }
        }

        // remove internal attributes and turn pre-visits into visits
        for (i = lst.size() - 1; i >= 0; i--) {
            obj = lst.get(i);
            if (obj instanceof Attribute) {
                lst.remove(i);
            } else if (obj instanceof Integer) {
                if (Visits.getPreNo(obj) == 0) {
                    // a visit that has happend implicitly before because to the
                    // bottom-up tree construction strategy:
                    if (Visits.getPreOfs(obj) == -1) {
                        throw new IllegalStateException();
                    }
                    lst.remove(i);
                } else {
                    lst.set(i, Visits.createVisit(Visits.getPreOfs(obj)));
                }
            } else if (obj instanceof Attribution) {
                // do nothing
            }
        }
        return new Visits(lst.toArray());
    }

    public Visits(Object[] visits) {
        this.visits = visits;
    }

    public Visits newInstance() {
        Object[] copy;
        Object obj;

        copy = new Object[visits.length];
        for (int i = visits.length - 1; i >= 0; i--) {
            obj = visits[i];
            if (obj instanceof Attribution) {
                copy[i] = ((Attribution) obj).newInstance();
            } else {
                copy[i] = obj;
            }
        }
        return new Visits(copy);
    }

    public Object get(int idx) {
        return visits[idx];
    }

    public int size() {
        return visits.length;
    }

    public static Object map(int prod, AttributeOccurrence ao, Ag sems, List<Attribute>[][] as) {
        AttributionBuffer ab;
        int symbol;
        int m;
        int no;
        int fx;

        if (sems.isInternal(ao.attr)) {
            return ao.attr;
        }
        ab = sems.findDefinition(prod, ao);
        if (ab != null) {
            return ab;
        }
        symbol = ao.attr.symbol;
        m = Util.find(as[symbol], ao.attr);
        if (m == -1) {
            throw new IllegalArgumentException("not found: " + ao.attr);
        }
        m++;  // the paper starts numbering with 1
        fx = as[symbol].length;  // BC case -- smalled odd >= m_x
        if ((fx & 1) == 0) {
            fx++;
        }
        no = (fx - m + 1) / 2;
        return createPreVisit(no, ao.ofs);
    }

    @Override
    public String toString() {
        StringBuilder buffer;
        int i;
        int max;
        Object obj;
        int no;
        int ofs;

        buffer = new StringBuilder();
        max = visits.length;
        for (i = 0; i < max; i++) {
            if (i > 0) {
                buffer.append(' ');
            }
            obj = visits[i];
            if (obj instanceof Integer) {
                ofs = getPreOfs(obj);
                no = getPreNo(obj);
                buffer.append(no);
                buffer.append(':');
                if (ofs == -1) {
                    buffer.append('^');
                } else {
                    buffer.append('!');
                    buffer.append(ofs);
                }
            } else {
                buffer.append(obj.toString());
            }
        }
        return buffer.toString();
    }

    public static Integer createVisit(int ofs) {
        return new Integer(ofs);
    }

    public static int getOfs(Object visit) {
        return ((Integer) visit).intValue();
    }

    /**
     * @param ofs -1 for left-hand side
     */
    public static Integer createPreVisit(int no, int ofs) {
        return new Integer((ofs << 16) + no);
    }

    public static int getPreNo(Object preVisit) {
        return ((Integer) preVisit).intValue() & 0xffff;
    }

    /**
     * @return -1 for left-hand side
     */
    public static int getPreOfs(Object preVisit) {
        return ((Integer) preVisit).intValue() >> 16;  // no >>> to keep sign
    }
}
