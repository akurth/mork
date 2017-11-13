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
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.parser.ParserTable;
import net.oneandone.sushi.util.IntBitSet;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Modes {
    public static void setNone(ParserTable table) {
        int max;
        char[] parserModes;

        max = table.getStateCount();
        parserModes = new char[max];  // initialized to 0
        table.setModes(parserModes);
    }

    public static List generate(FA fa, ParserTable table, IntBitSet whites, PrintWriter listing) throws GenericException {
        int i;
        int max;
        IntBitSet shifts;
        List<IntBitSet> modes;
        IntBitSet conflicts;
        char[] parserModes;

        modes = new ArrayList<IntBitSet>();
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

    private static char chooseState(FA fa, List<IntBitSet> modes, IntBitSet shifts) {
        int i;
        int max;
        IntBitSet test;

        max = modes.size();
        for (i = 0; i < max; i++) {
            test = modes.get(i);
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
