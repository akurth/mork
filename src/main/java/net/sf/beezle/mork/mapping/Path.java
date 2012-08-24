/**
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

import net.sf.beezle.mork.compiler.Syntax;
import net.sf.beezle.mork.grammar.Grammar;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.mork.semantics.AgBuffer;
import net.sf.beezle.mork.semantics.Attribute;
import net.sf.beezle.mork.semantics.Occurrence;
import net.sf.beezle.mork.semantics.Pusher;
import net.sf.beezle.mork.semantics.Type;
import net.sf.beezle.sushi.util.IntBitSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Visibility of some Definition, kind of an Argument builder.
 */
public class Path {
    public static final int DOWN = 0;
    public static final int DOWNS = 1;
    public static final int UP = 2;
    public static final int UPS = 3;

    /** stopper: yes; children: yes. */
    public static final int ISOLATED = 0;

    /** alternatives get merged. */
    public static final int MERGEABLE = 1;

    /**
     * @param targets   list of Definitions
     */
    public static void translate(Syntax syntax,
              Definition source, int move, IntBitSet stopper, List<Definition> targets, int modifier) throws GenericException {
        translate(syntax, modifier, source, targets,
                  new int[] { move }, new IntBitSet[] { new IntBitSet(stopper) });
    }

    /**
     * Creates a path with no steps
     */
    public static void translate(Syntax syntax, Definition source, Definition target) throws GenericException {
        List<Definition> targets;
        IntBitSet stopper;

        targets = new ArrayList<Definition>();
        targets.add(target);
        stopper = new IntBitSet();
        stopper.add(target.getAttribute().symbol);
        translate(syntax, ISOLATED, source, targets, new int[] {}, new IntBitSet[] { stopper });
    }

    /**
     * Creates a path with 1+ steps
     */
    public static void translate(Syntax syntax,
            Definition source, int[] moves, int[] symbols, Definition target, int modifier) throws GenericException {
        IntBitSet[] stoppers;
        List<Definition> targets;
        int i;

        if (moves.length - 1 != symbols.length) {
            throw new IllegalArgumentException();
        }

        stoppers = new IntBitSet[moves.length];
        targets = new ArrayList<Definition>();
        targets.add(target);
        for (i = 0; i < symbols.length; i++) {
            stoppers[i] = new IntBitSet();
            stoppers[i].add(symbols[i]);
        }
        stoppers[i] = new IntBitSet();
        stoppers[i].add(target.getAttribute().symbol);

        translate(syntax, modifier, source, targets, moves, stoppers);
    }

    /**
     * The method actually doing the work
     */
    private static void translate(Syntax syntax,
            int modifier, Definition source, List<Definition> targets, int[] moves, IntBitSet[] stoppers) throws GenericException {
        Path path;
        int count;

        path = new Path(syntax.getGrammar(), modifier, source, targets, moves, stoppers);
        count = path.translate();
        if (count == 0 && source.getAttribute().symbol != syntax.getGrammar().getStart()) {
            // TODO: improved error message
            throw new GenericException("dead-end path for attribute " + source.getName());
        }
    }


    //--

    private final Grammar grammar;

    private final Definition source;

    /** List of Definitions (in last stopper). */
    private final List<Definition> targets;

    // moves.length == stoppers.length == copyBuffers.length
    private final int[] moves;
    private final IntBitSet[] stoppers;
    private final int modifier;

    private final List<AgBuffer>[] copyBuffers;

    private Path(Grammar grammar,
                 int modifier, Definition source, List<Definition> targets, int[] moves, IntBitSet[] stoppers)
    {
        this.grammar = grammar;
        this.modifier = modifier;
        this.source = source;
        this.targets = targets;
        this.moves = moves;
        this.stoppers = stoppers;
        this.copyBuffers = new List[moves.length];
    }

    private int translate() throws GenericException {
        int step;
        int max;
        List<AgBuffer> buffers;
        Definition target;
        Argument arg;
        List<Definition> sources;

        buffers = new ArrayList<AgBuffer>();
        buffers.add(new AgBuffer(source.getAttribute()));
        for (step = 0; step < moves.length; step++) {
            translateStep(step, buffers);
            buffers = copyBuffers[step];
        }
        max = buffers.size();
        for (step = 0; step < max; step++) {
            sources = new ArrayList<Definition>();
            sources.add(source);
            arg = new Argument(modifier, buffers.get(step), sources);
            target = lookupTarget(arg.getAttribute().symbol);
            target.addArgument(arg, source);
        }
        return max;
    }

    private void translateStep(int step, List<AgBuffer> initialBuffers) {
        int i;
        int max;
        AgBuffer buffer;
        IntBitSet targetSymbols;

        if (step == moves.length - 1) {
            targetSymbols = getTargetSymbols(targets);
        } else {
            targetSymbols = stoppers[step];
        }

        copyBuffers[step] = new ArrayList<AgBuffer>();
        max = initialBuffers.size();
        for (i = 0; i < max; i++) {
            buffer = initialBuffers.get(i);
            prefixedTransport(step, buffer, stoppers[step], targetSymbols);
        }
    }

    private void prefixedTransport(
        int step, AgBuffer prefixBuffer, IntBitSet border, IntBitSet targetSymbols)
    {
        int ofs;
        int max;
        int i;
        Attribute oldAttr;
        Attribute newAttr;
        AgBuffer buffer;
        AgBuffer tmp;
        List<AgBuffer> resultBuffers;

        Attribute attr = prefixBuffer.getStart();
        resultBuffers = copyBuffers[step];
        ofs = resultBuffers.size();
        transport(grammar, attr, moves[step], border, targetSymbols, resultBuffers);
        max = resultBuffers.size();
        for (i = ofs; i < max; i++) {
            oldAttr = resultBuffers.get(i).getStart();
            tmp = new AgBuffer((Attribute) null);
            tmp.append(prefixBuffer);
            tmp.append(resultBuffers.get(i));
            buffer = new AgBuffer((Attribute) null);
            newAttr = buffer.cloneAttributes(tmp, oldAttr.type, oldAttr);
            buffer.setStart(newAttr);
            resultBuffers.set(i, buffer);
        }
    }

    private static IntBitSet getTargetSymbols(List<Definition> defs) {
        IntBitSet result;

        result = new IntBitSet();
        for (Definition def : defs) {
            result.add(def.getAttribute().symbol);
        }
        return result;
    }

    private Definition lookupTarget(int symbol) {
        for (Definition def : targets) {
            if (def.getAttribute().symbol == symbol) {
                return def;
            }
        }
        return null;
    }

    //--

    private static void transport(Grammar grammar, Attribute seed, int move, IntBitSet rawBorder,
                                  IntBitSet targetSymbols, List<AgBuffer> resultBuffers) {
        AgBuffer commulated;
        IntBitSet border;
        boolean down;
        List<Attribute> attrs;
        int i;
        int max;
        Attribute dest;
        AgBuffer tmp;
        int card;
        AgBuffer buffer;
        Attribute attr;
        Occurrence occ;

        if (move == Path.DOWN || move == Path.UP) {
            border = new IntBitSet();
            grammar.getSymbols(border);
        } else {
            border = rawBorder;
        }
        down = (move == Path.DOWN || move == Path.DOWNS);
        if (down && !border.contains(seed.symbol)) {
            // TODO: needed for \Block//VariableReference in compiler examples
            border.add(seed.symbol);
        }
        commulated = Pusher.run(down, seed, border, grammar);
        attrs = commulated.getTransportAttributes();
        max = attrs.size();
        for (i = 0; i < max; i++) {
            dest = attrs.get(i);
            if (dest != seed && targetSymbols.contains(dest.symbol)) {
                tmp = commulated.createReduced(dest);
                occ = null;
                if (down) {
                    card = tmp.isDownOptional()? Type.OPTION : Type.VALUE;
                    card = Type.cardCard(card, seed.type.card);
                } else {
                    occ = tmp.calcOccurrence(dest);
                    card = Type.cardCard(occ.card(), seed.type.card);
                    if (seed.type.card == Type.SEQUENCE || occ.max == Occurrence.UNBOUNDED) {
                        occ = null;  // don't split
                    }
                }
                if (occ == null) {
                    buffer = new AgBuffer((Attribute) null);
                    attr = buffer.cloneAttributes(tmp, new Type(seed.type.type, card), dest);
                    buffer.setStart(attr);
                    resultBuffers.add(buffer);
                } else {
                    createSplitted(tmp, seed.type.type, occ, dest, resultBuffers);
                }
            }
        }
    }

    private static void createSplitted(AgBuffer orig, Class<?> cls, Occurrence occ,
                                       Attribute origDest, List<AgBuffer> resultBuffers) {
        int seq;
        AgBuffer tmp;
        AgBuffer buffer;
        Attribute attr;
        Type type;
        Attribute dest;

        orig.calcOccurrences();
        for (seq = 0; seq < occ.max; seq++) {
            tmp = new AgBuffer((Attribute) null);
            dest = orig.createSequence(origDest, seq, tmp);

            if (seq < occ.min) {
                type = new Type(cls, Type.VALUE);
            } else {
                type = new Type(cls, Type.OPTION);
            }
            buffer = new AgBuffer((Attribute) null);
            attr = buffer.cloneAttributes(tmp, type, dest);
            buffer.setStart(attr);
            resultBuffers.add(buffer);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder;

        builder = new StringBuilder();
        for (IntBitSet stopper : stoppers) {
            builder.append(" ");
            builder.append(stopper);
        }
        return builder.toString();
    }
}
