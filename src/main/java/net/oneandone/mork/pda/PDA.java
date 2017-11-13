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
package net.oneandone.mork.pda;

import net.oneandone.mork.compiler.ConflictHandler;
import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.grammar.PrefixSet;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.parser.ParserTable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/* LR(k) automaton, follow the description in http://amor.cms.hu-berlin.de/~kunert/papers/lr-analyse/ */

public class PDA implements PDABuilder {
    public static PDA create(Grammar grammar, final Map<Integer, PrefixSet> firsts, final int k, int threadCount) {
        final PDA pda;
        State state;
        final Queue todo;
        int end;
        Thread[] threads;
        final List<Throwable> exceptions;

        threads = new Thread[threadCount];
        todo = new Queue(threads.length);
        state = State.forStartSymbol(grammar, grammar.getSymbolCount());
        state.closure(grammar, firsts, k);
        pda = new PDA(grammar, state);
        todo.put(state);
        exceptions = new ArrayList<Throwable>();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread("pda-builder-" + i) {
                @Override
                public void run() {
                    net.oneandone.mork.pda.State state;

                    try {
                        while (true) {
                            state = todo.take();
                            state.gotos(pda, firsts, todo, k);
                        }
                    } catch (InterruptedException e) {
                        return; // terminate
                    } catch (Throwable e) {
                        synchronized (exceptions) {
                            exceptions.add(e);
                        }
                    }
                }
            };
            threads[i].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }

        if (exceptions.size() > 0) {
            throw new IllegalStateException("thread exceptions: " + exceptions.size(), exceptions.get(0));
        }
        // TODO: hack hack hack
        end = pda.add(new State());
        pda.start.shifts.add(new Shift(grammar.getStart(), end));
        return pda;
    }

    private final Grammar grammar;
    private final HashMap<State, Integer> states;
    private final State start;

    public PDA(Grammar grammar, State start) {
        this.grammar = grammar;
        this.states = new LinkedHashMap<State, Integer>();
        this.start = start;
        add(start);
    }

    private Iterable<State> states() {
        return states.keySet();
    }

    public int add(State state) {
        int id;

        id = states.size();
        states.put(state, id);
        return id;
    }

    @Override
    public Grammar getGrammar() {
        return grammar;
    }

    /** @return id of existing state, -id of newly added state */
    @Override
    public synchronized int addIfNew(State state) {
        Integer existing;

        existing = states.get(state);
        return existing == null ? -add(state) : existing.intValue();
    }

    @Override
    public int size() {
        return states.size();
    }

    /**
     * Pseudo-symbol, indicates end-of-file (or an empty word if lookahead is seen as a set of words of length &lt;= 1)
     */
    public int getEofSymbol() {
        return grammar.getSymbolCount();
    }

    public ParserTable createTable(int lastSymbol, ConflictHandler handler) throws GenericException {
        // the initial syntax node created by the start action is ignored!
        ParserTable result;
        int eof;
        int end;

        eof = getEofSymbol();
        result = new ParserTable(0, size(), lastSymbol + 1 /* +1 for EOF */, eof, grammar, null);
        for (Map.Entry<State, Integer> entry : states.entrySet()) {
            entry.getKey().addActions(entry.getValue(), result, handler);
        }
        end = start.lookupShift(grammar.getStart()).end;
        result.addAccept(end, eof);
        return result;
    }

    public void print(PrintWriter dest) {
        for (Map.Entry<State, Integer> entry : states.entrySet()) {
            dest.println(entry.getKey().toString(entry.getValue(), grammar));
        }
    }

    public void statistics(PrintWriter output) {
        int size;
        int itemsCount;
        int itemsMin;
        int itemsMax;
        int lookaheadSizes;
        int lookaheadMin;
        int lookaheadMax;
        double hqMax;
        double hqMin;
        double hqSum;
        double loadMax;
        double loadMin;
        double loadSum;
        double q;

        itemsCount = 0;
        itemsMin = Integer.MAX_VALUE;
        itemsMax = Integer.MIN_VALUE;
        lookaheadMax = Integer.MIN_VALUE;
        lookaheadMin = Integer.MAX_VALUE;
        lookaheadSizes = 0;
        hqSum = 0;
        hqMax = Double.MIN_VALUE;
        hqMin = Double.MAX_VALUE;
        loadSum = 0;
        loadMax = Double.MIN_VALUE;
        loadMin = Double.MAX_VALUE;
        for (State state : states()) {
            size = state.items.size();
            itemsCount += size;
            itemsMin = Math.min(itemsMin, size);
            itemsMax = Math.max(itemsMax, size);
            for (Item item : state.items) {
                size = item.lookahead.size();
                lookaheadSizes += size;
                lookaheadMin = Math.min(lookaheadMin, size);
                lookaheadMax = Math.max(lookaheadMax, size);
                q = item.lookahead.hashQuality();
                hqSum += q;
                hqMax = Math.max(q, hqMax);
                hqMin = Math.min(q, hqMin);
                q = item.lookahead.load();
                loadSum += q;
                loadMax = Math.max(q, loadMax);
                loadMin = Math.min(q, loadMin);
            }
        }
        output.println("parser generation statistics");
        output.println("  states: " + states.size());
        output.println("  items avg: " + (itemsCount / states.size()) + ", min: " + itemsMin + ", max: " + itemsMax + ")");
        output.println("  lookahead avg: " + (lookaheadSizes / itemsCount) + ", min: " + lookaheadMin + ", max: " + lookaheadMax);
        output.println("  hash quality avg: " + (hqSum / itemsCount) + ", min: " + hqMin + ", max: " + hqMax);
        output.println("  hash load avg: " + (loadSum / itemsCount) + ", min: " + loadMin + ", max: " + loadMax);
        output.println("  heap size: " + Runtime.getRuntime().totalMemory());
        output.println("  heap used: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
    }
}
