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

import net.oneandone.mork.parser.Parser;
import net.oneandone.mork.parser.ParserTable;
import net.oneandone.mork.parser.TreeBuilder;
import net.oneandone.mork.scanner.Scanner;

import java.io.PrintStream;
import java.io.Serializable;

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


    //-- TreeBuilder interface

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
