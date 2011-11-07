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

package de.mlhartme.mork.mapping;

import java.util.ArrayList;
import java.util.List;

import de.mlhartme.mork.grammar.Grammar;
import de.mlhartme.mork.misc.GenericException;
import de.mlhartme.mork.reflect.Function;
import de.mlhartme.mork.reflect.Selection;
import de.mlhartme.mork.semantics.Ag;
import de.mlhartme.mork.semantics.Attribute;
import de.mlhartme.mork.semantics.AttributeOccurrence;
import de.mlhartme.mork.semantics.AttributionBuffer;
import de.mlhartme.mork.semantics.Type;

/**
 * Definition of an attribute,
 * Other possible names: seed, join, attachment, association, representation.
 */
public class Definition {
    public final String name;
    public final int symbol;
    private final boolean main;

    /** constructor reference. */
    public final Object constructor;

    private Attribute attribute;

    private List<Argument> isolated;
    private List<Argument> mergeable;

    // TODO: use {1}, {2} if GenericException support it
    public static final String ARGUMENT_NOT_ASSIGNABLE =
        "Attribute A is visible to attribute B, but B has no formal argument A is assignable to.";

    /**
     * @param constructor  Internal or Selection
     */
    public Definition(boolean main, Grammar grm, int symbol, String name, Object constructor) throws GenericException {
        Internal var;
        Type type;
        Selection sel;

        this.main = main;
        this.name = name;
        this.symbol = symbol;
        this.constructor = constructor;
        this.isolated = new ArrayList<Argument>();
        this.mergeable = new ArrayList<Argument>();

        if (constructor instanceof Internal) {
            var = (Internal) constructor;
            attribute = var.translate(symbol, grm);
        } else {
            sel = (Selection) constructor;
            type = new Type(sel.calcResult(), Type.VALUE);
            attribute = new Attribute(symbol, name, type);
        }
    }

    public void addArgument(Argument arg, Definition src) throws GenericException {
        Type type;

        type = arg.getAttribute().type;
        if (!Conversion.hasFormalArgument(getSelection(), type)) {
            throw new GenericException(ARGUMENT_NOT_ASSIGNABLE, "A=" + src.name + ", B=" + name
                                    + ", type of A=" + src.getAttribute().type.toString());
        }
        if (arg.getModifier() == Path.ISOLATED) {
            isolated.add(arg);
        } else {
            mergeable.add(arg);
        }
    }

    public boolean isMain() {
        return main;
    }

    public String getName() {
        return name;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Selection getSelection() {
        if (constructor instanceof Selection) {
            return (Selection) constructor;
        } else {
            return null;
        }
    }

    public void translate(Ag semantics, Transport transport, Grammar grammar) throws GenericException {
        Selection selection;
        Function fn;
        int prod, alt, maxAlt;
        int user, maxUser;
        List<Argument> args;
        List<Attribute> argAttrs;  // attributes from args, but re-arranged

        selection = getSelection();
        if (selection == null) {
            // Internal constructor, do nothing
            ((Internal) constructor).declare(attribute, semantics);
            return;
        }
        args = new ArrayList<Argument>();
        translateArguments(transport, semantics, args);
        argAttrs = new ArrayList<Attribute>();
        fn = Conversion.find(selection, this, args, argAttrs);
        if (grammar.isTerminal(attribute.symbol)) {
            // inherited attributes
            maxUser = grammar.getUserCount(attribute.symbol);
            for (user = 0; user < maxUser; user++) {
                prod = grammar.getUser(attribute.symbol, user);
                maxAlt = grammar.getUserOfsCount(attribute.symbol, user);
                for (alt = 0; alt < maxAlt; alt++) {
                    semantics.add(createAB(attribute, prod,
                            grammar.getUserOfs(attribute.symbol, user, alt), fn, argAttrs));
                }
            }
        } else {
            // synthesized attributes
            maxAlt = grammar.getAlternativeCount(attribute.symbol);
            for (alt = 0; alt < maxAlt; alt++) {
                semantics.add(createAB(attribute, grammar.getAlternative(attribute.symbol, alt), -1, fn, argAttrs));
            }
        }
    }

    private static AttributionBuffer createAB(Attribute result, int prod, int ofs, Function fn, List<Attribute> args) {
        AttributionBuffer ab;

        // all occurrences have the same ofs!
        ab = new AttributionBuffer(prod, fn, new AttributeOccurrence(result, ofs));
        for (Attribute a : args) {
            ab.add(new AttributeOccurrence(a, ofs));
        }
        return ab;
    }

    private void translateArguments(Transport transport, Ag semantics, List<Argument> result) throws GenericException {
        List<Argument> sorted;

        sorted = Argument.sortAndMergeArgs(this, mergeable);
        for (Argument arg : isolated) {
            arg.createTransport(semantics, transport);
            result.add(arg);
        }
        for (Argument arg : sorted) {
            arg.createTransport(semantics, transport);
            result.add(arg);
        }
    }

    @Override
    public String toString() {
        return "Definition " + name + " " + attribute;
    }
}
