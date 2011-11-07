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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Grammar. Make sure that data derived from productions (aka symbols) is
 * recomputed if the productions change.
 */
public class GrammarCore {
    /** List of int[] */
    private final List<int[]> productions;

    private Symbol[] symbols;

    public GrammarCore() {
        this.productions = new ArrayList<int[]>();
        this.symbols = null;
    }

    /**
     * @return number of productions.
     */
    public int getProductionCount() {
        return productions.size();
    }

    protected int[] getProduction(int prod) {
        return (int[]) productions.get(prod);
    }

    protected void addProduction(int[] prod) {
        productions.add(prod);
        symbols = null;
    }

    protected void addProduction(int ofs, int[] prod) {
        productions.add(ofs, prod);
        symbols = null;
    }

    protected void removeProduction(int idx) {
        productions.remove(idx);
        symbols = null;
    }

    protected Symbol[] getSymbols() {
        if (symbols == null) {
            calcSymbols();
        }
        return symbols;
    }

    protected Symbol getSymbol(int sym) {
        return getSymbols()[sym];
    }

    private void calcSymbols() {
        List<PreSymbol> pre;  // list of PreSymbols
        int prod, maxProd, ofs, sym;
        PreSymbol ps;
        int[] current;

        maxProd = productions.size();
        pre = new ArrayList<PreSymbol>();
        for (prod = 0; prod < maxProd; prod++) {
            current = getProduction(prod);
            for (ofs = 0; ofs < current.length; ofs++) {
                sym = current[ofs];
                while (pre.size() <= sym) {
                    pre.add(new PreSymbol());
                }
                ps = (PreSymbol) pre.get(sym);
                if (ofs == 0) {
                    ps.addAlternative(prod);
                } else {
                    ps.addUser(prod, ofs - 1);
                }
            }
        }
        symbols = new Symbol[pre.size()];
        for (sym = 0; sym < symbols.length; sym++) {
            symbols[sym] = ((PreSymbol) pre.get(sym)).createSymbol();
        }
    }
}
