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
package net.oneandone.mork.scanner;

import net.oneandone.mork.grammar.Rule;
import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.regexpr.Action;
import net.oneandone.mork.regexpr.ActionException;
import net.oneandone.mork.regexpr.Choice;
import net.oneandone.mork.regexpr.Loop;
import net.oneandone.mork.regexpr.Range;
import net.oneandone.mork.regexpr.RegExpr;
import net.oneandone.mork.regexpr.Sequence;
import net.oneandone.mork.regexpr.Without;
import net.oneandone.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.List;

/** stores the result from visiting a node */

public class Expander extends Action {
    private final Rule[] rules;

    /** symbols actually used for expanding */
    private final IntBitSet used;

    private final IntBitSet expanding;

    private final StringArrayList symbolTable;

    public Expander(Rule[] rules, StringArrayList symbolTable) {
        this.rules = rules;
        this.used = new IntBitSet();
        this.expanding = new IntBitSet();
        this.symbolTable = symbolTable;
    }

    public IntBitSet getUsed() {
        return used;
    }

    public RegExpr run(RegExpr re) throws ActionException {
        return (RegExpr) re.visit(this);
    }

    //--

    @Override
    public Object symbol(int symbol) throws ActionException {
        List<RegExpr> lst;
        int i;
        int max;
        RegExpr re;
        RegExpr[] args;

        if (expanding.contains(symbol)) {
            throw new ActionException("illegal recursion in scanner section for symbol " + symbolTable.getOrIndex(symbol));
        }
        used.add(symbol);

        lst = new ArrayList<RegExpr>();
        for (i = 0; i < rules.length; i++) {
            if (rules[i].getLeft() == symbol) {
                lst.add(rules[i].getRight());
            }
        }
        max = lst.size();
        if (max == 0) {
            throw new ActionException("illegal reference to parser symbol '" + symbolTable.getOrIndex(symbol)
                    + "' from scanner section");
        } else if (max == 1) {
            re = lst.get(0);
        } else {
            args = new RegExpr[max];
            lst.toArray(args);
            re = new Choice(args);
        }
        expanding.add(symbol);
        re = (RegExpr) re.visit(this);
        expanding.remove(symbol);
        return re;
    }

    @Override
    public Object range(char first, char last) {
        return new Range(first, last);
    }

    @Override
    public Object choice(Object[] body) {
        RegExpr[] args;

        args = new RegExpr[body.length];
        System.arraycopy(body, 0, args, 0, body.length);
        return new Choice(args);
    }

    @Override
    public Object sequence(Object[] body) {
        RegExpr[] args;

        args = new RegExpr[body.length];
        System.arraycopy(body, 0, args, 0, body.length);
        return new Sequence(args);
    }

    @Override
    public Object loop(Object rawBody) {
        return new Loop((RegExpr) rawBody);
    }

    @Override
    public Object without(Object left, Object right) {
        return new Without((RegExpr) left, (RegExpr) right);
    }
}
