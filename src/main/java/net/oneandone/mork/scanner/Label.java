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

import net.oneandone.sushi.util.IntBitSet;

/**
 * Label for states.
 */

public class Label {
    private IntBitSet symbols;

    public Label() {
        this.symbols = new IntBitSet();
    }
    public Label(int symbol) {
        symbols = new IntBitSet();
        symbols.add(symbol);
    }

    public IntBitSet getConflict(IntBitSet op) {
        IntBitSet result;

        result = new IntBitSet(symbols);
        result.retainAll(op);
        if (result.size() > 1) {
            return result;
        } else {
            return null;
        }
    }

    public static void resolveConflicts(FA fa, int[] prios) {
        int i;
        int max;
        Label label;

        max = fa.size();
        for (i = 0; i < max; i++) {
            label = (Label) fa.get(i).getLabel();
            if (label != null) {
                label.resolveConflicts(prios);
            }
        }
    }

    public void resolveConflicts(int[] prios) {
        int i;

        for (i = 0; i < prios.length; i++) {
            if (symbols.contains(prios[i])) {
                symbols = new IntBitSet();
                symbols.add(prios[i]);
                return;
            }
        }
        throw new RuntimeException();
    }

    public int getSymbol() {
        if (symbols.size() != 1) {
            throw new RuntimeException();
        }
        return symbols.first();
    }

    public int getSymbol(IntBitSet mode) {
        int sym;
        int result;

        result = - 1;
        for (sym = symbols.first(); sym != -1; sym = symbols.next(sym)) {
            if (mode.contains(sym)) {
                if (result != -1) {
                    throw new RuntimeException("ambiguous: " + result + " " + sym);
                }
                result = sym;
            }
        }
        return result;
    }

    public static boolean sameSymbols(FA fa, int siA, int siB) {
        Label a;
        Label b;

        a = (Label) fa.get(siA).getLabel();
        b = (Label) fa.get(siB).getLabel();
        return a.symbols.equals(b.symbols);
    }

    public static void combineLabels(FA front, FA back) {
        int si, max;
        State state;

        max = front.size();
        for (si = 0; si < max; si++) {
            state = front.get(si);
            state.setLabel(combineLabel(back, (IntBitSet) state.getLabel()));
        }
    }

    /** might return null */
    private static Label combineLabel(FA fa, IntBitSet states) {
        int si;
        Label tmp;
        Label result;

        result = null;
        for (si = states.first(); si != -1; si = states.next(si)) {
            tmp = (Label) fa.get(si).getLabel();
            if (tmp != null) {
                if (result == null) {
                    result = new Label();
                }
                result.symbols.addAll(tmp.symbols);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return symbols.toString();
    }
}
