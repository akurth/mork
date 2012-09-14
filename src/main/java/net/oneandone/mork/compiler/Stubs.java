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

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.grammar.Rule;
import net.oneandone.mork.mapping.Definition;
import net.oneandone.mork.mapping.Library;
import net.oneandone.mork.mapping.Path;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.reflect.Identity;
import net.oneandone.mork.reflect.Selection;
import net.oneandone.mork.regexpr.ActionException;
import net.oneandone.mork.regexpr.Range;
import net.oneandone.mork.regexpr.Sequence;
import net.oneandone.mork.semantics.BuiltIn;
import net.oneandone.mork.semantics.IllegalLiteral;
import net.oneandone.sushi.util.IntBitSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper functions referred by Mapper.mapper. These methods brigde gaps
 * between references in mork files and the functionality in the classes.
 * In the long run, this class should vanish as I provide more
 * and more powerful mapping features.
 */

public class Stubs {
    public static final String NO_SUCH_ATTRIBUTE
      = "No such attribute";
    public static final String NO_SUCH_SYMBOL_OR_ATTRIBUTE
      = "No such symbol or attribute";
    public static final String UNDEFINED_SYMBOL
      = "Undefined symbol";

    public static final String INVALID_CHARACTER = "invalid character code";

    public static final String ILLEGAL_INLINE = "defining attributes for inline symbols is illegal";

    //--

    // TODO
    public static Object sideEffect(List a, List b, List c, List d) {
        return null;
    }

    // TODO: invoke interface methods
    public static Grammar getIFGrammar(Syntax syntax) {
        return syntax.getGrammar();
    }
    // TODO: invoke interface methods
    public static StringArrayList getIFSymbolTable(Syntax syntax) {
        return syntax.getGrammar().getSymbolTable();
    }

    // TODO: as long as the context has no type I need this helper
    public static Syntax loadSyntax(Object mork, String fileName) throws GenericException, IllegalLiteral, IOException {
        return ((Mork) mork).loadSyntax(fileName);
    }

    public static char toChar(int num) throws GenericException {
        if ((num < 0) || (num > Character.MAX_VALUE)) {
            throw new GenericException(INVALID_CHARACTER, "" + num);
        }
        return (char) num;
    }

    public static int symbolRef(StringArrayList table, String name) throws GenericException {
        int result;

        result = table.indexOf(name);
        if (result == -1) {
            throw new GenericException(UNDEFINED_SYMBOL, name);
        }
        return result;
    }

    public static IntBitSet symbolSet(int[] lst) throws ActionException {
        IntBitSet result;
        int i, sym;

        result = new IntBitSet();
        for (i = 0; i < lst.length; i++) {
            sym = lst[i];
            if (result.contains(sym)) {
                throw new ActionException("duplicate symbol: " + sym);
            }
            result.add(sym);
        }
        return result;
    }

    // TODO: copy for arrays
    public static String[] keywordList(String[] keywords) {
        return keywords;
    }

    public static Definition createDefinition(
        Grammar grm, StringArrayList symbolTable, int symbol, String name, Object constr)
        throws GenericException {
        boolean main;
        IntBitSet terminals;

        main = (symbolTable.indexOf(name) == symbol);
        if (grm.isTerminal(symbol)) {
            terminals = new IntBitSet();
            grm.getUsedTerminals(terminals);
            // TODO: this computation is redundant to the computation of inline symbols in
            // GrammarSyntax. However, inline symbols are not available before the end of parsing ...
            if (!terminals.contains(symbol)) {
                throw new GenericException(ILLEGAL_INLINE);
            }
        }
        return new Definition(main, grm, symbol, name, constr);
    }

    public static Object step(int move, String symbolOrAttribute) {
        return new Object[] { new Integer(move), symbolOrAttribute };
    }

    private static int[] getMoves(Object[] steps) {
        int i;
        int[] moves;
        Object[] step;

        moves = new int[steps.length];
        for (i = 0; i < steps.length; i++) {
            step = (Object[]) steps[i];
            moves[i] = ((Integer) step[0]).intValue();
        }
        return moves;
    }

    private static String[] getSymbolsOrAttributes(Object[] steps) {
        int i;
        String[] sas;
        Object[] step;

        sas = new String[steps.length];
        for (i = 0; i < steps.length; i++) {
            step = (Object[]) steps[i];
            sas[i] = (String) step[1];
        }
        return sas;
    }

    public static Object implicitPath(Specification spec, Definition def) throws GenericException {
        spec.translateDefaultPushPath(def);
        return null; // TODO
    }

    public static Object localPath(Specification spec, Definition srcDef, int symbol, String name)
        throws GenericException {
        Definition user;

        user = spec.lookup(symbol, name);
        if (user == null) {
            throw new GenericException(NO_SUCH_ATTRIBUTE, name);
        }
        Path.translate(spec.getSyntax(), srcDef, user);
        return null; // TODO
    }

    public static Object normalPath(Definition source, Specification spec, Object[] steps) throws GenericException {
        int[] moves;
        int[] symbols;
        int i;
        String str;
        Definition target;
        String[] symbolsOrAttributes;
        StringArrayList symbolTable;

        symbolTable = spec.getSyntax().getGrammar().getSymbolTable();
        moves = getMoves(steps);
        symbolsOrAttributes = getSymbolsOrAttributes(steps);
        symbols = new int[symbolsOrAttributes.length - 1];
        for (i = 0; i < symbols.length; i++) {
            str = symbolsOrAttributes[i];
            target = spec.lookup(str);
            if (target != null) {
                symbols[i] = target.getAttribute().symbol;
            } else {
                symbols[i] = symbolTable.indexOf(str);
                if (symbols[i] == -1) {
                    throw new GenericException(NO_SUCH_SYMBOL_OR_ATTRIBUTE, str);
                }
            }
        }

        str = symbolsOrAttributes[symbolsOrAttributes.length - 1];
        target = spec.lookup(str);
        if (target == null) {
            throw new GenericException(NO_SUCH_ATTRIBUTE, str);
        }
        Path.translate(spec.getSyntax(), source, moves, symbols, target, Path.ISOLATED);
        return null; // TODO
    }

    public static Selection copyFunctionRef(Library lib, String name) throws GenericException {
        Selection sel;
        Class<?> type;
        Identity id;

        sel = lib.lookupClass(name);
        type = sel.calcResult();
        id = new Identity(name, type, type);
        return new Selection(id);
    }

    public static String name(String[] lst) {
        StringBuilder buf;
        int i;

        buf = new StringBuilder();
        for (i = 0; i < lst.length; i++) {
            if (i > 0) {
                buf.append('.');
            }
            buf.append(lst[i]);
        }
        return buf.toString();
    }

    /** second is a Character to detect optional values */
    public static Range range(char first, Character second) {
        if (second == null) {
            return new Range(first);
        } else {
            return new Range(first, ((Character) second).charValue());
        }
    }

    public static StringArrayList symbolTable(String[] a, String[] b) {
        StringArrayList result;

        result = new StringArrayList();
        add(result, a);
        add(result, b);

        return result;
    }

    public static void add(StringArrayList symbolTable, String[] symbols) {
        int i;
        String str;

        for (i = 0; i < symbols.length; i++) {
            str = symbols[i];
            if (symbolTable.indexOf(str) == -1) {
                symbolTable.add(str);
            }
        }
    }

    public static Syntax createSyntax(StringArrayList symbolTable, Rule[] parserRules,
            boolean prio, IntBitSet whiteSymbols, Rule[] explScannerRules, List rawKeywords) throws GenericException {
        return new Syntax(symbolTable, parserRules, prio, whiteSymbols, scannerRules(rawKeywords, explScannerRules));
    }

    public static Object[] keyword(int symbol, String name) throws IllegalLiteral {
        String str;

        str = BuiltIn.parseString(name);
        return new Object[] { new Integer(symbol), str };
    }

    public static Rule[] scannerRules(List rawKeywords, Rule[] rules) throws GenericException {
        List<Rule> keywords;
        IntBitSet doneSymbols;
        int i;
        int max;
        Rule[] all;
        Object[] pair;
        int symbol;
        StringArrayList doneKeywords;
        String kw;

        doneSymbols = new IntBitSet();
        doneKeywords = new StringArrayList();
        max = rawKeywords.size();
        keywords = new ArrayList<Rule>();
        for (i = 0; i < max; i++) {
            pair = (Object[]) rawKeywords.get(i);
            symbol = (Integer) pair[0];
            if (!doneSymbols.contains(symbol)) {
                doneSymbols.add(symbol);
                kw = (String) pair[1];
                if (doneKeywords.indexOf(kw) != -1) {
                    throw new GenericException("ambiguous String literal: " + kw);
                }
                keywords.add(new Rule(symbol, Sequence.createKeyword(kw)));
            }
        }
        all = new Rule[keywords.size() + rules.length];
        keywords.toArray(all);
        System.arraycopy(rules, 0,  all, keywords.size(),  rules.length);
        return all;
    }

    public static String attributeName(StringArrayList symbolTable, int recordSymbol, String attributeName) {
        if (attributeName != null) {
            return attributeName;
        } else {
            return symbolTable.get(recordSymbol);
        }
    }

    public static String attributeName(StringArrayList symbolTable, int recordSymbol) {
        return attributeName(symbolTable, recordSymbol, null);
    }
}
