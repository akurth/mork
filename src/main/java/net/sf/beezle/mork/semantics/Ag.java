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
package net.sf.beezle.mork.semantics;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Attribute grammar, supports >=0 synthesized and inherited attributes.
 */
public class Ag {
    private final Grammar grammar;

    /**
     * Attributes created from internal constructors. List of Attribute, Integer, Attribute, Integer, ...
     */
    private final List<Object> internals;

    private final List<AttributionBuffer> attributions;

    public Ag(Grammar grammar) {
        this.grammar = grammar;
        this.internals = new ArrayList<Object>();
        this.attributions = new ArrayList<AttributionBuffer>();
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public void add(AttributionBuffer ab) {
        attributions.add(ab);
    }

    // internal attribute
    public void add(Attribute a, int no) {
        internals.add(a);
        internals.add(no);
    }

    //--

    // firstAttrs are layed out first because the start symbol expects
    // them in a certain order
    public Oag createSemantics(List<Attribute> firstAttrs) throws GenericException {
        Layout layout;
        int[][] internalAttrs;
        Visits[] visits;

        layout = createLayout(firstAttrs);
        internalAttrs = createInternalAttributes(layout);
        visits = OagBuilder.run(this, layout, null);
        return new Oag(visits, internalAttrs);
    }

    private Layout createLayout(List<Attribute> firstAttrs) {
        int i;
        int max;
        Layout layout;

        layout = new Layout();
        max = firstAttrs.size();
        for (i = 0; i < max; i++) {
            layout.add(firstAttrs.get(i));
        }
        max = attributions.size();
        for (i = 0; i < max; i++) {
            layout.add(attributions.get(i));
        }
        // add scanner attrs if necessary; there may be unused ones.
        max = internals.size();
        for (i = 0; i < max; i += 2) {
            layout.add((Attribute) internals.get(i));
        }
        return layout;
    }

    private int[][] createInternalAttributes(Layout layout) {
        IntBitSet symbols;
        int s;
        int[] tmp;
        int i, max;
        int[][] internalAttrs; // result
        Attribute attr;
        Integer no;

        symbols = new IntBitSet();
        // make sure to include unused symbols:
        symbols.addRange(0, grammar.getSymbolCount() - 1);
        internalAttrs = new int[symbols.last() + 1][];
        for (s = symbols.first(); s != -1; s = symbols.next(s)) {
            tmp = new int[layout.getLocationCount(s)];
            for (i = 0; i < tmp.length; i++) {
                tmp[i] = NodeFactory.NONE;
            }
            internalAttrs[s] = tmp;
        }

        max = internals.size();
        for (i = 0; i < max; i += 2) {
            attr = (Attribute) internals.get(i);
            no = (Integer) internals.get(i + 1);
            internalAttrs[attr.symbol][layout.locate(attr)] = no.intValue();
        }
        return internalAttrs;
    }

    public int getSize() {
        return attributions.size();
    }

    public AttributionBuffer get(int i) {
        return attributions.get(i);
    }

    public void getProduction(int prod, Collection<AttributionBuffer> result) {
        int i;
        int max;
        AttributionBuffer ab;

        max = attributions.size();
        for (i = 0; i < max; i++) {
            ab = get(i);
            if (ab.production == prod) {
                result.add(ab);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer;
        int prod;
        int maxProds;
        int i;
        int max;
        AttributionBuffer ab;
        List<AttributionBuffer> tmp;

        buffer = new StringBuilder();
        maxProds = grammar.getProductionCount();
        tmp = new ArrayList<AttributionBuffer>();
        for (prod = 0; prod < maxProds; prod++) {
            buffer.append("[" + prod + "]\t");
            grammar.prodToString(buffer, prod);
            buffer.append("\n");

            tmp.clear();
            getProduction(prod, tmp);
            max = tmp.size();
            for (i = 0; i < max; i++) {
                ab = tmp.get(i);
                buffer.append("\t\t");
                ab.attrsToString(buffer, grammar.getSymbolTable());
                buffer.append('\n');
            }
            buffer.append("\n");
        }

        buffer.append("\n");
        return buffer.toString();
    }

    public AttributionBuffer findDefinition(int prod, AttributeOccurrence ao) {
        for (AttributionBuffer ab : attributions) {
            if (ab.production == prod && ab.result.equals(ao)) {
                return ab;
            }
        }
        return null;
    }

    /**
     * Besides the classic synthesized and inherited attributes, I have internal attributes.
     * Internal attributes are computed when creating the node, a better name might be 'initial'.
     */
    public void getAttributes(int symbol, Set<Attribute> internal, Set<Attribute> synthesized, Set<Attribute> inherited) {
        int i;
        int max;
        Attribute a;

        for (AttributionBuffer ab : attributions) {
            a = ab.result.attr;
            if (a.symbol == symbol) {
                if (ab.result.ofs == -1) {
                    synthesized.add(a);
                } else {
                    inherited.add(a);
                }
            }
        }
        max = internals.size();
        for (i = 0; i < max; i += 2) {
            a = (Attribute) internals.get(i);
            if (a.symbol == symbol) {
                internal.add(a);
            }
        }
    }

    public boolean isInternal(Attribute attr) {
        return internals.indexOf(attr) != -1;
    }
}
