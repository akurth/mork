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
package net.oneandone.mork.parser;

import net.oneandone.mork.compiler.ConflictResolver;
import net.oneandone.mork.mapping.ErrorHandler;
import net.oneandone.mork.scanner.Position;
import net.oneandone.mork.scanner.Scanner;
import net.oneandone.mork.scanner.ScannerFactory;
import net.oneandone.mork.semantics.SemanticError;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

/**
 * Push down automaton, performing LR parsing
 */

public class Parser {
    private final ParserTable table;
    private final ConflictResolver[] resolvers;
    private final ScannerFactory scannerFactory;
    private ErrorHandler errorHandler;

    // TODO: dynamically grow the Stack
    private static final int STACK_SIZE = 10240;

    private int top;          // index of the topmost state, might be -1
    private final int[] states;
    private final Object[] nodes;

    /** operand is one of the SPECIAL_xx values */
    public static final char SPECIAL = 0;
    public static final char SHIFT   = 1;
    public static final char REDUCE  = 2;
    public static final char SKIP    = 3;

    /** lowest two operand bits */
    public static final char SPECIAL_ERROR  = 0;
    public static final char SPECIAL_ACCEPT = 1;

    /** higher operand bits used to index resolver. */
    public static final char SPECIAL_CONFLICT = 2;

    static {
        if (SPECIAL_ERROR != 0) {
            throw new RuntimeException(
                "table packing relies on the fact that error states are code as 0");
        }
    }

    public Parser(ParserTable table, ConflictResolver[] resolvers, ScannerFactory scannerFactory) {
        this.table = table;
        this.resolvers = resolvers;
        this.scannerFactory = scannerFactory;
        this.errorHandler = null;

        states = new int[STACK_SIZE];
        nodes = new Object[STACK_SIZE];
        top = -1;
    }

    public ParserTable getTable() {
        return table;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public Parser newInstance() {
        return new Parser(table, resolvers, scannerFactory);
    }

    public Object run(Position position, Reader src, TreeBuilder treeBuilder, PrintStream verbose) throws IOException {
        int terminal;
        int production;
        int state;
        int value;
        Object node;
        Position pos;
        Scanner scanner;
        int operand;

        try {
            scanner = scannerFactory.newInstance(position, src);
            treeBuilder.open(scanner, this);
            try {
                state = table.getStartState();
                push(state, null);      // this state is never poped; thus, null is ok:
                while (true) {
                    terminal = scanner.next(table.getMode(state));
                    switch (terminal) {
                        case Scanner.ERROR:
                            pos = new Position();
                            scanner.getPosition(pos);
                            errorHandler.lexicalError(pos);
                            return null;
                        case Scanner.EOF:
                            terminal = table.getEofSymbol();
                            break;
                        default:
                            // normal terminal
                            break;
                    }
                lookupLoop:
                    while (true) {
                        value = table.lookup(state, terminal);
                        while (ParserTable.getAction(value) == SPECIAL) {
                            operand = ParserTable.getOperand(value);
                            switch (operand) {
                                case SPECIAL_ACCEPT:
                                    return pop();
                                case SPECIAL_ERROR:
                                    pos = new Position();
                                    scanner.getPosition(pos);
                                    errorHandler.syntaxError(pos, table.getShifts(state));
                                    return null;
                                default:
                                    if ((operand & 0x03) != SPECIAL_CONFLICT) {
                                        throw new IllegalStateException();
                                    }
                                    value = resolvers[operand >> 2].run(scanner, table.getMode(state), table.getEofSymbol());
                            }
                        }
                        switch (ParserTable.getAction(value)) {
                            case SHIFT:
                                if (verbose != null) {
                                    verbose.print(stateStr());
                                    verbose.println("shift " + ParserTable.getOperand(value));
                                }
                                state = ParserTable.getOperand(value);
                                push(state, treeBuilder.createTerminal(terminal));
                                break lookupLoop;
                            case REDUCE:
                                production = ParserTable.getOperand(value);
                                if (verbose != null) {
                                    verbose.print(stateStr());
                                    verbose.println("reduce " + production);
                                }

                                try {
                                    node = treeBuilder.createNonterminal(production);
                                } catch (SemanticError e) {
                                    errorHandler.semanticError(e.position, e.exception);
                                    return null;
                                }
                                // state != getState() because createNonterminnal removes nodes
                                state = table.lookupShift(getState(), production);
                                push(state, node);
                                break;
                            case SKIP:
                                if (verbose != null) {
                                    verbose.print(stateStr());
                                    verbose.println("skip " + terminal);
                                }
                                break lookupLoop;
                            default:
                                throw new RuntimeException();
                        }
                    }
                }
            } finally {
                top = -1;
            }
        } catch (IOException e) {
            throw new IOException(position.toString() + ": io error: " + e.getMessage(), e);
        }
    }

    private String stateStr() {
        StringBuilder builder;

        builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i <= top; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(states[i]);
        }
        builder.append("] ");
        return builder.toString();
    }

    /** returns the subject symbol of the production */
    public int getLeft(int production) {
        return table.getLeft(production);
    }

    public int getState() {
        return states[top];
    }

    public Object pop() {
        return nodes[top--];
    }

    public void push(int state, Object node) {
        top++;
        states[top] = state;
        nodes[top] = node;
    }
}
