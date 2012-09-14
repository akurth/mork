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
package net.oneandone.mork.mapping;

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.reflect.Function;
import net.oneandone.mork.reflect.Selection;
import net.oneandone.mork.semantics.Ag;
import net.oneandone.mork.semantics.Attribute;
import net.oneandone.mork.semantics.AttributeOccurrence;
import net.oneandone.mork.semantics.AttributionBuffer;
import net.oneandone.mork.semantics.Type;

import java.util.ArrayList;
import java.util.List;

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

    private final List<Argument> isolated;
    private final List<Argument> mergeable;

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
