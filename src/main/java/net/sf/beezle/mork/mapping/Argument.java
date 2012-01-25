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

package net.sf.beezle.mork.mapping;

import net.sf.beezle.mork.semantics.Ag;
import net.sf.beezle.mork.semantics.AgBuffer;
import net.sf.beezle.mork.semantics.Attribute;
import net.sf.beezle.mork.semantics.Compare;
import net.sf.beezle.mork.semantics.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * A translated path.
 */
public class Argument implements Compare {
    /**
     * The attribute actually passed to the function -- this attribute is the result of
     * the transport (or: the root of the transport tree)
     */
    private final Attribute attr;

    /** how to combine this argument with others */
    private final int modifier;

    /** attribution to copy source attributes into attr to be passed to attr. */
    private final AgBuffer copyBuffer;

    /** list of definitions */
    private final List<Definition> sources;

    /** non-local argument */
    public Argument(int modifier, AgBuffer buffer, List<Definition> sources) {
        this.modifier = modifier;
        this.attr = buffer.getStart();
        this.copyBuffer = buffer;
        this.sources = sources;
    }

    public void createTransport(Ag dest, Transport transport) {
        // adding scanner attributes is not requires since Arguments have not scanner attributes
        copyBuffer.createSemanticsBuffer(dest, transport);
    }

    public int getModifier() {
        return modifier;
    }

    public Attribute getAttribute() {
        return attr;
    }

    public int compare(Argument right) {
        return copyBuffer.compare(right.copyBuffer);
    }

    /**
     * Merge list of arguments.
     * pre: list.size() > 0  && all arguments have to start with the same attribute
     */
    public static Argument merge(int symbol, Definition target, List<Argument> arguments) {
        AgBuffer buffer;
        List<AgBuffer> argBuffers;
        int i;
        int max;
        Attribute start;
        Argument arg;
        Type mergedType;
        int card;
        List<Definition> resultingSources;

        max = arguments.size();
        if (max == 0) {
            throw new IllegalArgumentException();
        }
        argBuffers = new ArrayList<AgBuffer>();
        mergedType = null;
        resultingSources = new ArrayList<Definition>();
        for (i = 0; i < max; i++) {
            arg = arguments.get(i);
            resultingSources.addAll(arg.sources);
            if (arg == null) {
                throw new IllegalStateException();
            }
            argBuffers.add(arg.copyBuffer);
            if (mergedType == null) {
                mergedType = arg.attr.type;
            } else {
                mergedType = mergedType.alternate(arg.attr.type);
            }
        }
        if (mergedType == null) {
            throw new IllegalStateException();
        }
        // compute with card SEQUENCE. TODO: very expensive
        buffer = new AgBuffer((Attribute) null);
        start = buffer.merge(argBuffers, symbol, new Type(mergedType.type, Type.SEQUENCE));
        card = buffer.calcCard(start);
        buffer = new AgBuffer((Attribute) null);
        // TODO: duplicate computation ...
        start = buffer.merge(argBuffers, symbol, new Type(mergedType.type, card));
        buffer.setStart(start);
        return new Argument(Path.ISOLATED, buffer, resultingSources);
    }

    //--

    /**
     * @param args  List of Arguments
     */
    public static List<Argument> sortAndMergeArgs(Definition target, List<Argument> args) {
        List<Argument> seq;
        int i;
        int max;
        Argument arg;
        List<Argument> mergable;
        List<List<Argument>> sort;

        max = args.size();
        seq = new ArrayList<Argument>();
        mergable = new ArrayList<Argument>();
        for (i = 0; i < max; i++) {
            arg = args.get(i);
            if (arg.modifier == Path.MERGEABLE) {
                mergable.add(arg);
            } else {
                seq.add(arg);
            }
        }
        sort = RelatedArgument.sort(mergable);
        max = sort.size();
        for (i = 0; i < max; i++) {
            arg = merge(target.getAttribute().symbol, target, sort.get(i));
            seq.add(arg);
        }
        return seq;
    }

    @Override
    public String toString() {
        return "{{arg start=" + attr + "\n" + copyBuffer.toString() + "}}";
    }

    public String getSourcesString() {
        StringBuilder result;
        int i;
        int max;
        Definition def;

        result = new StringBuilder("(source attributes:");
        max = sources.size();
        for (i = 0; i < max; i++) {
            result.append(' ');
            def = (Definition) sources.get(i);
            result.append(def.getName());
            result.append(" (type=");
            result.append(def.getAttribute().type.toString());
            result.append(')');
        }
        result.append(')');
        return result.toString();
    }
}
