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

package net.sf.beezle.mork.scanner;

import net.sf.beezle.mork.grammar.IllegalSymbol;
import net.sf.beezle.mork.grammar.Rule;
import net.sf.beezle.mork.misc.StringArrayList;
import net.sf.beezle.mork.regexpr.Action;
import net.sf.beezle.mork.regexpr.ActionException;
import net.sf.beezle.mork.regexpr.Choice;
import net.sf.beezle.mork.regexpr.Loop;
import net.sf.beezle.mork.regexpr.Range;
import net.sf.beezle.mork.regexpr.RegExpr;
import net.sf.beezle.mork.regexpr.Sequence;
import net.sf.beezle.mork.regexpr.Without;
import net.sf.beezle.sushi.util.IntBitSet;

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

    public RegExpr run(RegExpr re) throws IllegalSymbol, ActionException {
        return (RegExpr) re.visit(this);
    }

    //-----------------------------------------------------------------

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
