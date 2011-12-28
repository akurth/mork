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

package net.sf.beezle.mork.grammar;

import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.regexpr.Action;
import net.sf.beezle.mork.regexpr.ActionException;

/**
 * <p>Translate regular expressions to context free grammars.</p>
 *
 * <p>Note: The Grammar start symbol is always expandable (i.e.: it is never used at the right-hand-side).
 * Recursion usually expands the right-hand-side symbol.</p>
 */
public class Ebnf extends Action {
    /** helper symbols are added without gaps, starting with freeHelper. */
    public static Grammar translate(Rule[] rules, StringArrayList symbolTable) throws ActionException {
        int i;
        Ebnf builder;
        Grammar tmp;
        Grammar buffer;
        int firstHelper;
        int lastHelper;

        firstHelper = symbolTable.size();
        buffer = new Grammar(rules[0].getLeft(), symbolTable);
        builder = new Ebnf(firstHelper);
        for (i = 0; i < rules.length; i++) {
            tmp = (Grammar) rules[i].getRight().visit(builder);
            buffer.add(rules[i].getLeft(), tmp.getStart());
            buffer.addProductions(tmp);
            buffer.expandSymbol(tmp.getStart());
            buffer.removeProductions(tmp.getStart());
        }
        lastHelper = builder.getHelper() - 1;
        buffer.removeDuplicateSymbols(firstHelper, lastHelper);
        buffer.removeDuplicateRules();
        buffer.packSymbols(firstHelper, lastHelper + 1);
        return buffer;
    }

    //--

    private int helper;   // Helper symbols

    public Ebnf(int helper) {
        this.helper = helper;
    }

    public int getHelper() {
        return helper;
    }

    @Override
    public Object choice(Object[] body) {
        Grammar result;
        Grammar tmp;
        int i;

        result = new Grammar(helper);
        for (i = 0; i < body.length; i++) {
            tmp = (Grammar) body[i];
            result.add(helper, tmp.getStart());
            result.addProductions(tmp);
            result.expandSymbol(tmp.getStart());
            result.removeProductions(tmp.getStart());
        }
        helper++;
        return result;
    }

    @Override
    public Object sequence(Object[] body) {
        Grammar result;
        Grammar tmp;
        int[] right;
        int i;

        result = new Grammar(helper);
        right = new int[body.length];
        for (i = 0; i < body.length; i++) {
            tmp = (Grammar) body[i];
            right[i] = tmp.getStart();
        }
        result.add(helper, right);
        for (i = 0; i < body.length; i++) {
            tmp = (Grammar) body[i];
            result.addProductions(tmp);
            result.expandSymbol(tmp.getStart());
            result.removeProductions(tmp.getStart());
        }
        helper++;

        return result;
    }

    @Override
    public Object loop(Object rawBody) {
        Grammar result;
        Grammar body;

        body = (Grammar) rawBody;
        result = new Grammar(helper + 1);
        result.add(helper + 1, helper);

        // generate left-recursive rule, even though this would
        // cause problems for LL parsers
        result.add(helper,   helper, body.getStart());
        result.add(helper,   body.getStart());

        result.addProductions(body);
        result.expandSymbol(body.getStart());
        result.removeProductions(body.getStart());
        helper += 2;

        return result;
    }

    @Override
    public Object symbol(int symbol) {
        Grammar result;

        result = new Grammar(helper);
        result.add(helper, symbol);
        helper++;
        return result;
    }

    @Override
    public Object range(char first, char last) throws ActionException {
        if (first == last) {
            throw new ActionException("illegal character literal in parser section: " +
                "char code=" + (int) first +
                ":\nuse a string literal (double quotes instead of single quotes!)");
        } else {
            throw new ActionException("illegal range in parser section: "
                                             + (int) first + ".." + (int) last +
                ":\ndefine a helper symbol for this range in the scanner section and use\n" +
                "the helper symbol instead. ");
        }
    }

    @Override
    public Object without(Object left, Object right) throws ActionException {
        throw new ActionException("illegal without operator");
    }
}
