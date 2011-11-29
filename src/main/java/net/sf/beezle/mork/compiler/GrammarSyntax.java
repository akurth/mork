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

import net.sf.beezle.sushi.util.IntBitSet;

import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.grammar.GrammarBuilder;
import net.sf.beezle.mork.grammar.Rule;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.parser.Conflicts;
import net.sf.beezle.mork.parser.PDA;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.scanner.FABuilder;
import net.sf.beezle.mork.scanner.GrammarScannerFactory;
import net.sf.beezle.mork.scanner.Modes;

/**
 * Grammar syntax specification. Represents a grammar syntax file with
 * parser and scanner section.
 *
 * Design issues: whitespace handling is performed in the parser, not in the
 * scaner. This is because I might a features access the "[text]" of white space.
 */

public class GrammarSyntax extends Syntax {
    private Grammar grammar;
    private boolean priorities;
    private IntBitSet whiteSymbols;
    private Rule[] scannerRules;

    public GrammarSyntax(
        StringArrayList symbolTable, Rule[] parserRules,
        boolean priorities, IntBitSet whiteSymbols, Rule[] scannerRules)
        throws GenericException {
        if (parserRules.length == 0) {
            throw new GenericException("empty parser section, at least one rule is needed");
        }
        this.grammar = GrammarBuilder.createGrammar(parserRules, symbolTable);
        this.priorities = priorities;
        if (whiteSymbols != null) {
            this.whiteSymbols = whiteSymbols;
        } else {
            this.whiteSymbols = new IntBitSet();
        }
        this.scannerRules = scannerRules;
    }

    @Override
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Translate specification.
     *
     * @return null for errors.
     */
    @Override
    public Parser translate(Output output) throws GenericException {
        FABuilder builder;
        Conflicts conflicts;
        PDA pda;
        ParserTable parserTable;
        int eof;
        GrammarScannerFactory scannerFactory;
        IntBitSet usedTerminals;
        IntBitSet usedSymbols;
        int symbolCount;
        StringArrayList symbolTable;

        output.verbose("processing parser section");

        pda = new PDA(grammar, grammar.getStart());
        conflicts = new Conflicts();
        symbolCount = Math.max(grammar.getSymbolCount(), whiteSymbols.last() + 1);
        parserTable = pda.createTable(conflicts, symbolCount);
        parserTable.addWhitespace(whiteSymbols, conflicts);
        symbolTable = grammar.getSymbolTable();
        if (!conflicts.isEmpty()) {
            output.error("TODO", LALR_CONFLICT + conflicts.toString(symbolTable));
        }
        if (output.listing != null) {
            output.listing.println("\nSymbols:");
            output.listing.println(symbolTable.toString());
            output.listing.println("\nGrammar:");
            output.listing.println(grammar.toString());
            output.listing.println("\nAutomaton:");
            pda.print(grammar, output.listing);
        }
        output.statistics();
        output.statistics("parser statistics");
        output.statistics("  states: " + pda.size());
        output.statistics("  table: [symbols=" + parserTable.getSymbolCount()
                      + "][states=" + parserTable.getStateCount() + "]");
        eof = pda.eof;

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
        scannerFactory = GrammarScannerFactory.create(
            builder.getFA(), builder.getErrorState(), parserTable, whiteSymbols, output.verbose, output.listing, eof);

        output.statistics();
        output.statistics("scanner statistics");
        output.statistics("  fa states : " + builder.getFA().size());
        output.statistics("  table: char[" + scannerFactory.size() + "]");
        output.verbose("scanner done");

        usedSymbols = new IntBitSet(whiteSymbols);
        usedSymbols.addAll(builder.getInlines());
        grammar.check(grammar.getStart(), usedSymbols, symbolTable.toList());

        return new Parser(parserTable, scannerFactory);
    }
}
