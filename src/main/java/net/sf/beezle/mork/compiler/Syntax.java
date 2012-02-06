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

package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.grammar.Ebnf;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.grammar.PrefixSet;
import net.sf.beezle.mork.grammar.Rule;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.pda.PDA;
import net.sf.beezle.mork.scanner.FABuilder;
import net.sf.beezle.mork.scanner.Modes;
import net.sf.beezle.mork.scanner.ScannerFactory;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.Map;

/**
 * Grammar syntax specification. Represents a grammar syntax file with
 * parser and scanner section.
 *
 * Design issues: whitespace handling is performed in the parser, not in the
 * scanner. This is because I might a features access the "[text]" of white space.
 */
public class Syntax {
    public static final String CONFLICT = "conflicts (use the -lst option to obtain a listing of the automaton):\n";

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
    public Parser translate(int k, Output output) throws GenericException {
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
        output.verbose("creating pda");
        pda = PDA.create(grammar, firsts, k);
        output.verbose("done: " + pda.size() + " states, " + (System.currentTimeMillis() - started) + " ms");
        symbolCount = Math.max(grammar.getSymbolCount(), whiteSymbols.last() + 1);
        handler = new ConflictHandler(pda);
        parserTable = pda.createTable(symbolCount, handler);
        parserTable.addWhitespace(whiteSymbols, handler);
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
            output.statistics.println("  k>1 used: " + handler.conflicts());
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
