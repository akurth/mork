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
package net.oneandone.mork.grammar;

import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.regexpr.Action;
import net.oneandone.mork.regexpr.ActionException;

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
            buffer.addProduction(new int[]{rules[i].getLeft(), tmp.getStart()});
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
            result.addProduction(new int[]{helper, tmp.getStart()});
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
        int[] prod;
        int i;

        result = new Grammar(helper);
        prod = new int[body.length + 1];
        prod[0] = helper;
        for (i = 0; i < body.length; i++) {
            tmp = (Grammar) body[i];
            prod[i + 1] = tmp.getStart();
        }
        result.addProduction(prod);
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
        result.addProduction(new int[]{helper + 1, helper});

        // generate left-recursive rule, even though this would
        // cause problems for LL parsers
        result.addProduction(new int[]{helper, helper, body.getStart()});
        result.addProduction(new int[]{helper, body.getStart()});

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
        result.addProduction(new int[]{helper, symbol});
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
