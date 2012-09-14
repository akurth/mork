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
package net.oneandone.mork.scanner;

import net.oneandone.mork.grammar.Rule;
import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.regexpr.Action;
import net.oneandone.mork.regexpr.ActionException;
import net.oneandone.mork.regexpr.Choice;
import net.oneandone.mork.regexpr.Range;
import net.oneandone.mork.regexpr.RegExpr;
import net.oneandone.sushi.util.IntBitSet;

import java.io.PrintStream;

/** Translates Rules into an FA */
public class FABuilder extends Action {
    /**
     * Translates only those rules where the left-hand.side is contained
     * in the specified terminals set. The remaining rules are used for inlining.
     */
    public static FABuilder run(Rule[] rules, IntBitSet terminals, StringArrayList symbolTable, PrintStream verbose)
            throws ActionException {
        FA alt;
        int i;
        Expander expander;
        FABuilder builder;
        Label label;
        RegExpr expanded;
        Minimizer minimizer;

        expander = new Expander(rules, symbolTable);
        builder = new FABuilder(symbolTable);
        builder.fa = (FA) new Choice().visit(builder);
        if (verbose != null) {
            verbose.println("building NFA");
        }
        for (i = 0; i < rules.length; i++) {
            if (terminals.contains(rules[i].getLeft())) {
                expanded = (RegExpr) rules[i].getRight().visit(expander);
                alt = (FA) expanded.visit(builder);
                label = new Label(rules[i].getLeft());
                alt.setEndLabels(label);
                builder.fa.alternate(alt);
            }
        }
        if (verbose != null) {
            verbose.println("building DFA");
        }
        builder.fa = DFA.create(builder.fa);
        if (verbose != null) {
            verbose.println("complete DFA");
        }
        builder.errorSi = builder.fa.add(null);
        builder.fa = CompleteFA.create(builder.fa, builder.errorSi);
        if (verbose != null) {
            verbose.println("minimized DFA");
        }
        minimizer = new Minimizer(builder.fa);
        builder.fa = minimizer.run();
        builder.errorSi = minimizer.getNewSi(builder.errorSi);
        builder.inlines = expander.getUsed();
        if (builder.fa.isEnd(builder.fa.getStart())) {
            label = (Label) builder.fa.get(builder.fa.getStart()).getLabel();
            throw new ActionException("Scanner accepts the empty word for symbol " + symbolTable.get(label.getSymbol())
                    + ".\nThis is illegal because it might cause infinite loops when scanning.");
        }
        return builder;
    }

    //--

    // result variables
    private FA fa;
    private int errorSi;
    private IntBitSet inlines;

    // temporary state during run()
    private StringArrayList symbolTable;


    public FA getFA() {
        return fa;
    }

    public IntBitSet getInlines() {
        return inlines;
    }

    public int getErrorState() {
        return errorSi;
    }

    //--

    private FABuilder(StringArrayList symbolTable) {
        this.symbolTable = symbolTable;

        // errorSi and fa will be assigned by run():
    }

    //-- action methods

    @Override
    public Object symbol(int symbol) throws ActionException {
        throw new ActionException("illegal symbol in scanner section: " + symbolTable.getOrIndex(symbol));
    }

    @Override
    public Object range(char first, char last) {
        int start, end;  // state indexes
        FA fa;

        fa = new FA();
        start = fa.add(null);
        fa.setStart(start);
        end = fa.add(null);
        fa.setEnd(end);
        fa.get(start).add(end, new Range(first, last));

        return fa;
    }

    @Override
    public Object choice(Object[] body) {
        FA result;
        FA tmp;
        int i;

        result = new FA();
        i = result.add(null);
        result.setStart(i);
        // don't set end state
        for (i = 0; i < body.length; i++) {
            tmp = (FA) body[i];
            result.alternate(tmp);
        }
        return result;
    }

    @Override
    public Object sequence(Object[] body) {
        FA result;
        FA tmp;
        int i;

        result = new FA();
        i = result.add(null);
        result.setStart(i);
        result.setEnd(i);
        for (i = 0; i < body.length; i++) {
            tmp = (FA) body[i];
            result.sequence(tmp);
        }
        return result;
    }

    @Override
    public Object loop(Object rawBody) {
        FA body;

        body = (FA) rawBody;
        body.plus();
        return body;
    }

    @Override
    public Object without(Object a, Object b) {
        FA result;

        // A \ B = A and not(B) = not(not(A and not(B))) = not(not(a) or B)

        result = DFA.create((FA) a);
        result.not();
        result.alternate((FA) b);
        result = DFA.create((FA) result);
        result.not();

        return result;
    }
}
