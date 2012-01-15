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

/** Immutable, heavily shared between PrefixSets. */
public class Prefix implements Comparable<Prefix> {
    private static final int BASE = 1024;
    
    public static Prefix EMPTY = new Prefix(0);

    public static Prefix forSymbol(int symbol) {
        if (symbol >= BASE - 1) {
            throw new IllegalArgumentException("" + symbol);
        }
        return new Prefix(symbol + 1);
    }

    private final long data;

    private Prefix(long data) {
        this.data = data;
    }

    public int first() {
        long remaining;
        long next;

        remaining = data;
        while (true) {
            next = remaining / BASE;
            if (next == 0) {
                return (int) remaining - 1;
            }
            remaining = next;
        }
    }

    public Prefix concat(Prefix right, int k) {
        int[] rightSymbols;
        int size;
        long newData;
        
        if (data == 0) {
            return right;
        }
        if (right.data == 0) {
            return this;
        }
        rightSymbols = right.symbols();
        size = size();
        newData = data;
        for (int i = size(); i < k; i++) {
            if (i - size >= rightSymbols.length) {
                break;
            }
            newData = newData * BASE + rightSymbols[i - size] + 1;
        }
        return new Prefix(newData);
    }

    public int size() {
        long remaining;
        int size;
        
        for (size = 0, remaining = data; remaining != 0; remaining /= BASE) {
            size++;
        }
        return size;
    }

    private int[] symbols() {
        int[] result;
        long remaining;

        remaining = data;
        result = new int[size()];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = (int) (remaining % BASE) - 1;
            remaining /= BASE;
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder;
        int[] symbols;

        symbols = symbols();
        builder = new StringBuilder();
        for (int i = 0; i < symbols.length; i++) {
            builder.append(' ');
            builder.append(symbols[i]);
        }
        return builder.toString();
    }

    public void toString(StringArrayList symbolTable, StringBuilder result) {
        int[] symbols;
        
        symbols = symbols();
        for (int i = 0; i < symbols.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(symbolTable.getOrIndex(symbols[i]));
        }
    }

    public int[] follows(int first) {
        int[] terminals;
        int[] symbols;

        symbols = symbols();
        if (symbols.length > 0) {
            if (symbols[0] == first) {
                terminals = new int[symbols.length - 1];
                for (int i = 0; i < terminals.length; i++) {
                    terminals[i] = symbols[(i + 1)];
                }
                return terminals;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return (int) (data / (BASE - 1)); 
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Prefix) {
            return eq((Prefix) obj);
        }
        return false;
    }

    public boolean eq(Prefix operand) {
        return data == operand.data;
    }

    @Override
    public int compareTo(Prefix operand) {
        return (int) (data - operand.data);
    }
}
