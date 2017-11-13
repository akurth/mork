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

import net.oneandone.sushi.util.IntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for Grammar. Make sure that data derived from productions (aka symbols) is
 * recomputed if the productions change.
 */
public class GrammarBase {
    /** List of int[] */
    private final List<int[]> productions;

    private Symbol[] symbols;

    public GrammarBase() {
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
        return productions.get(prod);
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
                ps = pre.get(sym);
                if (ofs == 0) {
                    ps.addAlternative(prod);
                } else {
                    ps.addUser(prod, ofs - 1);
                }
            }
        }
        symbols = new Symbol[pre.size()];
        for (sym = 0; sym < symbols.length; sym++) {
            symbols[sym] = pre.get(sym).createSymbol();
        }
    }

    //--

    /**
     * Helper class to instantiate symbols.
     */
    public static class PreSymbol {
        /** productions for this symbol */
        private final IntArrayList alternatives;

        /** productions using this symbol. */
        private final IntArrayList users;

        /** ofsets in the using productions. List of IntArrayLists. */
        private final List<IntArrayList> userOfs;

        public PreSymbol() {
            alternatives = new IntArrayList();
            users = new IntArrayList();
            userOfs = new ArrayList<IntArrayList>();
        }

        public void addAlternative(int prod) {
            alternatives.add(prod);
        }

        public void addUser(int prod, int ofs) {
            int i;
            IntArrayList ofsArray;

            i = users.indexOf(prod);
            if (i == -1) {
                i = users.size();
                users.add(prod);
                userOfs.add(new IntArrayList());
            }

            ofsArray = userOfs.get(i);
            ofsArray.add(ofs);
        }

        public Symbol createSymbol() {
            int[][] ofss;
            int i;
            IntArrayList ofsArray;

            ofss = new int[users.size()][];
            for (i = 0; i < ofss.length; i++) {
                ofsArray = userOfs.get(i);
                ofss[i] = ofsArray.toArray();
            }

            return new Symbol(alternatives.toArray(), users.toArray(), ofss);
        }
    }
}
