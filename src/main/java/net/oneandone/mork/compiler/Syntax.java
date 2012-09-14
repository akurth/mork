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
package net.oneandone.mork.compiler;

import net.oneandone.mork.grammar.Ebnf;
import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.grammar.PrefixSet;
import net.oneandone.mork.grammar.Rule;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.parser.Parser;
import net.oneandone.mork.parser.ParserTable;
import net.oneandone.mork.pda.PDA;
import net.oneandone.mork.scanner.FABuilder;
import net.oneandone.mork.scanner.Modes;
import net.oneandone.mork.scanner.ScannerFactory;
import net.oneandone.sushi.util.IntBitSet;

import java.util.Map;

/**
 * Grammar syntax specification. Represents a grammar syntax file with
 * parser and scanner section.
 *
 * Design issues: whitespace handling is performed in the parser, not in the
 * scanner. This is because I might a features access the "[text]" of white space.
 */
public class Syntax {
    public static final String CONFLICT = "lr(k) conflict:\n";

    private Grammar grammar;
    private boolean priorities;
    private IntBitSet whiteSymbols;
    private Rule[] scannerRules;

    public Syntax(StringArrayList symbolTable, Rule[] parserRules, boolean priorities, IntBitSet whiteSymbols, Rule[] scannerRules) throws GenericException {
        if (parserRules.length == 0) {
            throw new IllegalArgumentException();
        }
        this.grammar = Ebnf.translate(parserRules, symbolTable);
        this.priorities = priorities;
        if (whiteSymbols != null) {
            this.whiteSymbols = whiteSymbols;
        } else {
            this.whiteSymbols = new IntBitSet();
        }
        this.scannerRules = scannerRules;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Translate specification.
     *
     * @return null for errors.
     */
    public Parser translate(int k, int threadCount, Output output) throws GenericException {
        FABuilder builder;
        long started;
        PDA pda;
        ParserTable parserTable;
        Map<Integer, PrefixSet> firsts;
        ScannerFactory scannerFactory;
        IntBitSet usedTerminals;
        IntBitSet usedSymbols;
        int symbolCount;
        StringArrayList symbolTable;
        ConflictHandler handler;
        ConflictResolver[] resolvers;

        started = System.currentTimeMillis();
        output.verbose("computing firsts");
        firsts = grammar.firsts(k);
        output.verbose("creating pda, " + threadCount + " threads");
        pda = PDA.create(grammar, firsts, k, threadCount);
        output.verbose("done: " + pda.size() + " states, " + (System.currentTimeMillis() - started) + " ms");
        symbolCount = Math.max(grammar.getSymbolCount(), whiteSymbols.last() + 1);
        handler = new ConflictHandler(grammar);
        parserTable = pda.createTable(symbolCount, handler);
        parserTable.addWhitespace(whiteSymbols);
        symbolTable = grammar.getSymbolTable();
        if (output.listing != null) {
            output.listing.println("\nSymbols:");
            output.listing.println(symbolTable.toString());
            output.listing.println("\nGrammar:");
            output.listing.println(grammar.toString());
            output.listing.println("\nFirst Sets:");
            for (Map.Entry<Integer, PrefixSet> entry : firsts.entrySet()) {
                output.listing.println(symbolTable.get(entry.getKey()) + ":\t" + entry.getValue().toString(symbolTable));
            }
            output.listing.println("\nAutomaton:");
            pda.print(output.listing);
        }
        if (output.statistics != null) {
            output.statistics.println();
            output.statistics.println("parser statistics");
            output.statistics.println("  states: " + pda.size());
            output.statistics.println("  table: [symbols=" + parserTable.getSymbolCount() + "][states=" + parserTable.getStateCount() + "]");
            output.statistics.println("  lr(1) conflicts: " + handler.resolvers());
            pda.statistics(output.statistics);
        }

        resolvers = handler.report(output, grammar);

        // free memory before computing FA
        pda = null;

        output.verbose("processing scanner section");

        usedTerminals = new IntBitSet();
        grammar.getUsedTerminals(usedTerminals);
        usedTerminals.addAll(whiteSymbols);

        output.verbose("generating scanner");
        builder = FABuilder.run(scannerRules, usedTerminals, symbolTable, output.verbose);
        output.listing("inline symbols: " + builder.getInlines());

        if (priorities) {
            output.verbose("use priorities");
            Modes.resolveScannerConflicts(builder.getFA(), scannerRules);
        }
        scannerFactory = ScannerFactory.create(
            builder.getFA(), builder.getErrorState(), parserTable, whiteSymbols, output.verbose, output.listing);

        if (output.statistics != null) {
            output.statistics.println();
            output.statistics.println("scanner statistics");
            output.statistics.println("  fa states : " + builder.getFA().size());
            output.statistics.println("  table: char[" + scannerFactory.size() + "]");
        }
        output.verbose("scanner done");

        usedSymbols = new IntBitSet(whiteSymbols);
        usedSymbols.addAll(builder.getInlines());
        grammar.check(grammar.getStart(), usedSymbols, symbolTable.toList());

        return new Parser(parserTable, resolvers, scannerFactory);
    }
}
