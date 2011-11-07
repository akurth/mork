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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.beezle.sushi.util.IntBitSet;

import net.sf.beezle.mork.grammar.Rule;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.parser.ParserTable;

public class Modes {
    public static void setNone(ParserTable table) throws GenericException {
        int max;
        char[] parserModes;

        max = table.getStateCount();
        parserModes = new char[max];  // initialized to 0
        table.setModes(parserModes);
    }

    public static List generate(FA fa, ParserTable table, IntBitSet whites, PrintStream listing) throws GenericException {
        int i;
        int max;
        IntBitSet shifts;
        List modes;  // scanner modes: list of set of symbols
        IntBitSet conflicts;
        char[] parserModes;

        modes = new ArrayList();
        if (hasConflicts(fa, whites) != null) {
            throw new GenericException("scanner conflict in whitespace");
        }
        max = table.getStateCount();
        parserModes = new char[max];
        for (i = 0; i < max; i++) {
            shifts = table.getShifts(i);
            shifts.addAll(whites);
            conflicts = hasConflicts(fa, shifts);
            if (conflicts != null) {
                throw new GenericException("scanner conflict in state " + i + ": " + conflicts);
            }
            parserModes[i] = chooseState(fa, modes, shifts);
        }
        if (listing != null) {
            listing.println("scanner modes: " + modes.size());
            for (i = 0; i < modes.size(); i++) {
                listing.println(" mode " + i + " " + modes.get(i));
            }
            listing.println("modes for parser states: ");
            for (i = 0; i < max; i++) {
                listing.println(" " + i + " " + (int) parserModes[i]);
            }
        }
        table.setModes(parserModes);
        return modes;
    }

    private static char chooseState(FA fa, List modes, IntBitSet shifts) {
        int i;
        int max;
        IntBitSet test;

        max = modes.size();
        for (i = 0; i < max; i++) {
            test = (IntBitSet) modes.get(i);
            test = new IntBitSet(test);
            test.addAll(shifts);
            if (hasConflicts(fa, test) == null) {
                modes.set(i, test);
                return (char) i;
            }
        }
        // create a new scanner state
        modes.add(new IntBitSet(shifts));
        return (char) (modes.size() - 1);
    }

    private static IntBitSet hasConflicts(FA fa, IntBitSet symbols) {
        int si;
        int max;
        Label label;
        IntBitSet conflict;

        max = fa.size();
        for (si = 0; si < max; si++) {
            label = (Label) fa.get(si).getLabel();
            if (label != null) {
                conflict = label.getConflict(symbols);
                if (conflict != null) {
                    return conflict;
                }
            }
        }
        return null;
    }

    public static void resolveScannerConflicts(FA fa, Rule[] rules) {
        int[] prios;
        int i;

        prios = new int[rules.length];
        for (i = 0; i < prios.length; i++) {
            prios[i] = rules[i].getLeft();
        }
        Label.resolveConflicts(fa, prios);
    }
}
