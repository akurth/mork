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

package net.sf.beezle.mork.semantics;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.sushi.graph.EdgeIterator;
import net.sf.beezle.sushi.graph.Graph;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Build visit sequence for ordered attribute grammar. Based on the paper
 * Uwe Kastens: "Ordered Attribute Grammars", Acta Informatics, 1980.
 */
public class OagBuilder {
    private final Ag semantics;
    private final Layout layout;
    private final Grammar grammar; // TODO: use grammarBuffer instead
    private final IntBitSet symbols;

    public OagBuilder(Ag semantics, Layout layout) {
        this.semantics = semantics;
        this.layout = layout;
        this.grammar = semantics.getGrammar();
        this.symbols = new IntBitSet();
        grammar.getSymbols(symbols);
    }

    //--------

    public static Visits[] run(Ag ag, Layout layout, boolean verbose) throws GenericException {
        OagBuilder builder;
        Graph[] dp;
        Graph[] idp;
        Graph[] ids;
        int i;
        List[][] as;
        StringArrayList symbolTable;
        Graph[] ds;
        Graph[] edp;
        Visits[] visits;

        builder = new OagBuilder(ag, layout);
        dp = builder.createDP();
        idp = builder.createIDP(dp);
        ids = builder.createIDS(idp);
        as = builder.createA(ids);
        ds = builder.createDS(ids, as);
        edp = builder.createEDP(dp, ds);
        visits = builder.createVisits(edp, as);
        if (verbose) {
            symbolTable = ag.getGrammar().getSymbolTable();
            for (i = 0; i < dp.length; i++) {
                System.out.println("prod=" + i);
                System.out.println("  dp\t" + aos(symbolTable, dp[i]));
                System.out.println("  idp\t" + aos(symbolTable, idp[i]));
                System.out.println("  edp\t" + aos(symbolTable, edp[i]));
            }
            for (i = 0; i < ids.length; i++) {
                System.out.println(symbolTable.get(i) + ":");
                System.out.println(" ids\t" + as(symbolTable, ids[i]));
                System.out.println(" as\t");
                print(as[i]);
                System.out.println(" ds\t" + as(symbolTable, ds[i]));
            }
        }
        return visits;
    }

    private static void print(List[] as) {
        int i;
        int j;
        int max;

        for (i = 0; i < as.length; i++) {
            System.out.print("\t\t" + i + ":");
            max = as[i].size();
            for (j = 0; j < max; j++) {
                System.out.print(' ');
                System.out.print(((Attribute) as[i].get(j)).name);
            }
            System.out.println();
        }
    }

    private static String as(StringArrayList symbolTable, Graph relation) {
        StringBuilder buffer;
        EdgeIterator iter;
        Attribute a;

        buffer = new StringBuilder();
        buffer.append('{');
        iter = relation.edges();
        while (iter.step()) {
            buffer.append(" (");
            a = (Attribute) iter.left();
            buffer.append(a.toString(symbolTable));
            buffer.append(", ");
            a = (Attribute) iter.right();
            buffer.append(a.toString(symbolTable));
            buffer.append(") ");
        }
        buffer.append('}');
        return buffer.toString();
    }

    private static String aos(StringArrayList symbolTable, Graph relation) {
        StringBuilder buffer;
        EdgeIterator iter;
        AttributeOccurrence ao;

        buffer = new StringBuilder();
        buffer.append('{');
        iter = relation.edges();
        while (iter.step()) {
            buffer.append(" (");
            ao = (AttributeOccurrence) iter.left();
            buffer.append(ao.toString(symbolTable));
            buffer.append(", ");
            ao = (AttributeOccurrence) iter.right();
            buffer.append(ao.toString(symbolTable));
            buffer.append(") ");
        }
        buffer.append('}');
        return buffer.toString();
    }


    /**
     * Computes "dependency" relation DP (definition 1).
     *
     * @return array indexed by productions where each Relation contains pairs of
     *         AttributeOccurrences.
     */
    public Graph[] createDP() {
        Graph[] dp;
        int i;
        int max;
        AttributionBuffer ab;

        dp = new Graph[semantics.getGrammar().getProductionCount()];
        for (i = 0; i < dp.length; i++) {
            dp[i] = new Graph();
        }
        max = semantics.getSize();
        for (i = 0; i < max; i++) {
            ab = (AttributionBuffer) semantics.get(i);
            addDP(dp[ab.production], ab);
        }
        return dp;
    }

    private void addDP(Graph dp, AttributionBuffer ab) {
        int i;
        int max;

        max = ab.getArgCount();
        for (i = 0; i < max; i++) {
            dp.addEdge(ab.getArg(i), ab.result);
        }
    }

    /**
     * Computes "induced dependency" releation IDP (definition 2).
     * The relation contains pairs of AttributeOcurrences.
     *
     * @param dp dependency relation
     */
    public Graph[] createIDP(Graph[] dp) {
        Graph[] idp;
        Graph[] idpClosure;
        boolean[] touched;
        int p;
        int q;
        AttributeOccurrence left;
        AttributeOccurrence right;
        int symbol;
        int ofs;
        boolean modified;
        AttributeOccurrence newLeft;
        AttributeOccurrence newRight;
        EdgeIterator iter;

        idp = new Graph[dp.length];
        idpClosure = new Graph[dp.length];
        touched = new boolean[dp.length];
        for (p = 0; p < idp.length; p++) {
            idp[p] = new Graph();
            idp[p].addGraph(dp[p]);
            idpClosure[p] = new Graph();
            idpClosure[p].addGraph(dp[p]);
            idpClosure[p].closureHere();
            touched[p] = true;
        }

        do {
            modified = false;
            for (q = 0; q < idp.length; q++) {
                if (touched[q]) {
                    touched[q] = false;
                    iter = idpClosure[q].edges();
                    while (iter.step()) {
                        left = (AttributeOccurrence) iter.left();
                        right = (AttributeOccurrence) iter.right();
                        if (left.sameSymbolOccurrence(right)) {
                            for (p = 0; p < idp.length; p++) {
                                for (ofs = 0; ofs <= grammar.getLength(p); ofs++) {
                                    symbol = semantics.getGrammar().getSymbol(p, ofs);
                                    if (symbol == left.attr.symbol) {
                                        newLeft = new AttributeOccurrence(left.attr, ofs - 1);
                                        newRight = new AttributeOccurrence(right.attr, ofs - 1);
                                        if (idp[p].addEdge(newLeft, newRight)) {
                                            idpClosure[p].addEdge(newLeft, newRight);
                                            idpClosure[p].closureHere();
                                            touched[p] = true;
                                            modified = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } while (modified);
        return idp;
    }

    /**
     * Computes "induced dependencies between attributes of symbols" IDS (Definition 3).
     *
     * @return Array indexed by symbols; relation with pairs of attributes.
     */
    public Graph[] createIDS(Graph[] idp) {
        int p;
        int i;
        Graph[] ids;
        AttributeOccurrence left;
        AttributeOccurrence right;
        EdgeIterator iter;

        ids = new Graph[symbols.last() + 1];
        for (i = 0; i < ids.length; i++) {
            ids[i] = new Graph();
        }
        for (p = 0; p < idp.length; p++) {
            iter = idp[p].edges();
            while (iter.step()) {
                left = (AttributeOccurrence) iter.left();
                right = (AttributeOccurrence) iter.right();
                if (left.sameSymbolOccurrence(right)) {
                    ids[left.attr.symbol].addEdge(left.attr, right.attr);
                }
            }
        }
        return ids;
    }

    /**
     * Computes Axy. (Definition 4).
     */
    public List[][] createA(Graph[] ids) throws GenericException {
        Set<Attribute> internal;
        Set<Attribute> synthesized;
        Set<Attribute> inherited;
        int i;
        List[][] result;

        internal = new HashSet<Attribute>();
        inherited = new HashSet<Attribute>();
        synthesized = new HashSet<Attribute>();
        result = new List[ids.length][];
        for (i = 0; i < ids.length; i++) {
            internal.clear();
            inherited.clear();
            synthesized.clear();
            semantics.getAttributes(i, internal, synthesized, inherited);
            synthesized.addAll(internal);
            result[i] = Partition.createA(synthesized, inherited, ids[i]);
        }
        return result;
    }

    /**
     * Computes DS, the completion of IDS using A. (Definition 5).
     */
    public Graph[] createDS(Graph[] ids, List[][] a) {
        int i;
        Graph[] ds;

        ds = new Graph[ids.length];
        for (i = 0; i < ds.length; i++) {
            ds[i] = createDSx(ids[i], a[i]);
        }
        return ds;
    }

    private Graph createDSx(Graph ids, List[] a) {
        Graph ds;
        int i;
        List leftList;
        int leftSize;
        List rightList;
        int rightSize;
        int left;
        int right;

        ds = new Graph();
        ds.addGraph(ids);
        for (i = 1; i < a.length; i++) {
            leftList = a[i];
            leftSize = leftList.size();
            rightList = a[i - 1];
            rightSize = rightList.size();
            for (left = 0; left < leftSize; left++) {
                for (right = 0; right < rightSize; right++) {
                    ds.addEdge(leftList.get(left), rightList.get(right));
                }
            }
        }
        return ds;
    }

    public Graph[] createEDP(Graph[] dp, Graph[] ds) {
        int i;
        Graph[] eds;

        eds = new Graph[dp.length];
        for (i = 0; i < eds.length; i++) {
            eds[i] = createEDPx(i, dp[i], ds);
        }
        return eds;
    }

    private Graph createEDPx(int p, Graph dp, Graph[] ds) {
        Graph edsP;
        int ofs;
        int maxOfs;
        int symbol;
        EdgeIterator iter;

        edsP = new Graph();
        edsP.addGraph(dp);
        maxOfs = semantics.getGrammar().getLength(p);
        for (ofs = 0; ofs <= maxOfs; ofs++) {
            symbol = semantics.getGrammar().getSymbol(p, ofs);
            iter = ds[symbol].edges();
            while (iter.step()) {
                edsP.addEdge(new AttributeOccurrence((Attribute) iter.left(), ofs - 1),
                          new AttributeOccurrence((Attribute) iter.right(), ofs - 1));
            }
        }
        return edsP;
    }

    public Visits[] createVisits(Graph[] edp, List[][] as) throws GenericException {
        int p;
        Visits[] visits;

        visits = new Visits[edp.length];
        for (p = 0; p < edp.length; p++) {
            visits[p] = Visits.forEDP(p, edp[p], semantics, as, layout);
        }
        return visits;
    }
}
