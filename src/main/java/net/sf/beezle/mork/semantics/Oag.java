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

import java.io.PrintStream;
import java.io.Serializable;

import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.parser.TreeBuilder;
import net.sf.beezle.mork.scanner.Scanner;

/**
 * Ordered attribute grammar.
 */
public class Oag implements TreeBuilder, Serializable {
    private transient PrintStream logging;
    private transient NodeFactory[] terminals;
    private transient NodeFactory[] nonterminals;
    private transient Scanner scanner;
    private transient Parser parser;
    private transient Object environment;

    /**
     * Index by production.
     */
    private final Visits[] visits;

    // [symbol][attr]   attributes computed when constructing a node
    private final int[][] internalAttrs;

    public Oag(Visits[] visits, int[][] internalAttrs) {
        this.visits = visits;
        this.internalAttrs = internalAttrs;
        this.logging = null;
        this.terminals = null;
        this.nonterminals = null;
    }

    public void setEnvironment(Object environment) {
        this.environment = environment;
    }

    public void setLogging(PrintStream logging) {
        this.logging = logging;
    }

    public Oag newInstance() {
        Oag oag;

        oag = new Oag(visits, internalAttrs);
        oag.setLogging(logging);
        return oag;
    }

    public void open(Scanner scanner, Parser parser) {
        this.scanner = scanner;
        this.parser = parser;
        initFactories();
    }

    private void initFactories() {
        int i;
        ParserTable table;

        if (nonterminals != null) {
            return;
        }
        table = parser.getTable();
        nonterminals = new NodeFactory[visits.length];
        for (i = 0; i < nonterminals.length; i++) {
            nonterminals[i] =
                new NodeFactory(10, table.getLength(i), internalAttrs[table.getLeft(i)], visits[i]);
        }
        terminals = new NodeFactory[internalAttrs.length];
        for (i = 0; i < terminals.length; i++) {
            terminals[i] = new NodeFactory(5, internalAttrs[i]);
        }
    }


    //-------------------------------------------------------------
    // TreeBuilder interface

    public Object createTerminal(int terminal) {
        return terminals[terminal].allocateTerminal(scanner, environment);
    }

    public Object createNonterminal(int production) throws SemanticError {
        Node node;

        node = nonterminals[production].allocateNonterminal(parser, environment);
        node.compute(logging);
        return node;
    }

    public void printVisits(PrintStream dest) {
        int i;

        for (i = 0; i < visits.length; i++) {
            dest.print("prod ");
            dest.print(i);
            dest.print(": ");
            dest.print(visits[i].toString());
            dest.println();
        }
    }
}
