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

package net.sf.beezle.mork.xml;

// TODO
import java.util.ArrayList;
import java.util.List;

import net.sf.beezle.sushi.util.IntBitSet;

import net.sf.beezle.mork.compiler.Output;
import net.sf.beezle.mork.compiler.Syntax;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.grammar.GrammarBuilder;
import net.sf.beezle.mork.grammar.Rule;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.parser.Conflicts;
import net.sf.beezle.mork.parser.PDA;
import net.sf.beezle.mork.parser.Parser;
import net.sf.beezle.mork.parser.ParserTable;
import net.sf.beezle.mork.regexpr.Choice;
import net.sf.beezle.mork.regexpr.RegExpr;
import net.sf.beezle.mork.regexpr.Sequence;
import net.sf.beezle.mork.regexpr.Symbol;
import net.sf.beezle.mork.scanner.Modes;

/**
 * Represents a DTD file. The translate method results in a Parser and an XmlScanner,
 * both of them operating on Xml token.
 */
public class XmlSyntax extends Syntax {
    private Grammar grammar;

    /** [element (==symbol)][] */
    private Attribute[][] attrs;

    public XmlSyntax(Object[] ruleOrAttribute, StringArrayList symbolTable) throws GenericException {
        Object[] obj;
        Rule[] rules;

        obj = separate(symbolTable, ruleOrAttribute);
        rules = (Rule[]) obj[0];
        this.attrs = (Attribute[][]) obj[1];
        if (rules.length == 0) {
            throw new GenericException("DTD must define at least one element");
        }
        this.grammar = GrammarBuilder.createGrammar(rules, symbolTable);
        grammar.check(grammar.getStart(), new IntBitSet(), symbolTable.toList());
    }

    private static Object[] separate(StringArrayList symbolTable, Object[] ruleOrAttribute) {
        List a;
        List r;
        Rule[] rules;
        Attribute[][] attrs;
        Object obj;
        int i;
        List lst;

        a = new ArrayList();
        r = new ArrayList();
        for (i = 0; i < ruleOrAttribute.length; i++) {
            obj = ruleOrAttribute[i];
            if (obj != null) {
                if (obj instanceof Object[]) {
                    r.add(obj);
                } else {
                    addAttrs((Attribute) obj, a);
                }
            } else {
                // ignore - comment
            }
        }
        attrs = new Attribute[a.size()][];
        for (i = 0; i < attrs.length; i++) {
            lst = (List) a.get(i);
            attrs[i] = new Attribute[lst.size()];
            lst.toArray(attrs[i]);
        }

        rules = new Rule[r.size()];
        for (i = 0; i < rules.length; i++) {
            rules[i] = createRule(symbolTable, (Object[]) r.get(i), attrs);
        }
        return new Object[] { rules, attrs };
    }

    private static void addAttrs(Attribute a, List attrs) {
        int ele;
        List lst;

        ele = a.getElement();
        while (attrs.size() <= ele) {
            attrs.add(new ArrayList());
        }
        lst = (List) attrs.get(ele);
        lst.add(a);
    }

    private static Rule createRule(StringArrayList symbolTable, Object[] ruleRaw, Attribute[][] allAttrs) {
        RegExpr content;
        String name;
        int element;
        int start;
        int end;
        RegExpr[] seq;
        int i;
        Attribute[] attrs;
        String tmp;

        name = (String) ruleRaw[0];
        content = (RegExpr) ruleRaw[1];
        element = symbolTable.indexOf(name);
        if (element == -1) {
            throw new RuntimeException("element name not defined");
        }
        if (element >= allAttrs.length) {
            attrs = new Attribute[0];
        } else {
            attrs = allAttrs[element];
        }

        tmp = toStartTag(name);
        start = symbolTable.size();
        symbolTable.add(tmp);
        tmp = toEndTag(name);
        end = symbolTable.size();
        symbolTable.add(tmp);

        seq = new RegExpr[1 + attrs.length + 1 + 1];
        seq[0] = new Symbol(start);
        for (i = 0; i < attrs.length; i++) {
            seq[i + 1] = new Symbol(attrs[i].getTerminal());
            if (attrs[i].isOptional()) {
                seq[i + 1] = Choice.createOption(seq[i + 1]);
            }
        }
        seq[seq.length - 2] = content;
        seq[seq.length - 1] = new Symbol(end);

        return new Rule(element, new Sequence(seq));
    }


    public static String toStartTag(String element) {
        return '<' + element + '>';
    }
    public static String toEndTag(String element) {
        return "</" + element + '>';
    }

    public static int toStartTag(StringArrayList symbolTable, int element) {
        String name;

        name = symbolTable.get(element);
        return symbolTable.indexOf(toStartTag(name));
    }
    public static int toEndTag(StringArrayList symbolTable, int element) {
        String name;

        name = symbolTable.get(element);
        return symbolTable.indexOf(toEndTag(name));
    }

    public static String toAttribute(String element, String attr) {
        return element + "$" + attr;
    }

    public static int toAttribute(StringArrayList symbolTable, String element, String attr) {
        return symbolTable.indexOf(toAttribute(element, attr));
    }

    @Override
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Translate dtd represented by this object.
     *
     * @return != null.
     */
    @Override
    public Parser translate(Output output) throws GenericException {
        Conflicts conflicts;
        PDA pda;
        ParserTable table;
        int eof;
        StringArrayList symbolTable;

        output.verbose("processing parser section");
        symbolTable = grammar.getSymbolTable();
        pda = new PDA(grammar, grammar.getStart());
        conflicts = new Conflicts();
        table = pda.createTable(conflicts, grammar.getSymbolCount());
        Modes.setNone(table);
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
        output.statistics("  table: [symbols=" + table.getSymbolCount()
                          + "][states=" + table.getStateCount() + "]");

        return new Parser(table, new XmlScannerFactory(symbolTable, pda.getEofSymbol(), attrs));
    }
}
