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

import de.mlhartme.mork.reflect.Composition;
import de.mlhartme.mork.reflect.Function;
import de.mlhartme.mork.reflect.Method;
import de.mlhartme.mork.reflect.Option;
import de.mlhartme.mork.semantics.Attribute;
import de.mlhartme.mork.semantics.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class (one instance per Mapping) to create functions for transport attribution.
 */
public class Transport {
    private final Function fnCopy;
    private final Function fnCreateOption;
    private final Function fnCreateSequence;
    private final Function fnCreateSequenceValue;
    private final Function fnCreateSequenceOption;
    private final Function fnSequenceAndValue;
    private final Function fnSequenceAndOption;
    private final Function fnSequenceAndSequence;

    public Transport() {
        fnCreateOption = get("createOption");
        fnCreateSequence = get("createSequence");
        fnCreateSequenceValue = get("createSequenceValue");
        fnCreateSequenceOption = get("createSequenceOption");
        fnSequenceAndValue = get("sequenceAndValue");
        fnSequenceAndOption = get("sequenceAndOption");
        fnSequenceAndSequence = get("sequenceAndSequence");
        fnCopy = get("copy");
    }

    private static Function get(String name) {
        Function fn;

        fn = Method.forName(Transport.class, name).getFunction();
        if (fn == null) {
            throw new RuntimeException("not found: " + name);
        }
        return fn;
    }

    //----------------------------------------------------------------

    /**
     * @param src   list of attributes
     */
    public Function getTransportFn(List<Attribute> src, int destCard) {
        Function tmp;
        int i, max, srcCard;
        String msg;

        switch (destCard) {
            case Type.OPTION:
                switch (src.size()) {
                    case 0:
                        return fnCreateOption;
                    case 1:
                        return fnCopy;
                    default:
                        msg = "no optional transport for this number of arguments: " + src.size();
                        throw new RuntimeException(msg);
                }
            case Type.VALUE:
                if (src.size() != 1) {
                    msg = "no value transport for this number of arguments: " + src.size();
                    throw new RuntimeException(msg);
                }
                return fnCopy;
            case Type.SEQUENCE:
                max = src.size();
                if (max == 0) {
                    return fnCreateSequence;
                } else {
                    srcCard = src.get(0).type.card;
                    tmp = getInitialSequence(srcCard);
                    for (i = 1; i < max; i++) {
                        srcCard = src.get(i).type.card;
                        tmp = Composition.create(getSequenceAnd(srcCard), 0, tmp);
                    }
                    return tmp;
                }
            default:
                throw new RuntimeException();
        }
    }

    private Function getInitialSequence(int cardinality) {
        switch (cardinality) {
            case Type.VALUE:
                return fnCreateSequenceValue;
            case Type.OPTION:
                return fnCreateSequenceOption;
            case Type.SEQUENCE:
                return fnCopy;
            default:
                throw new RuntimeException();
        }
    }

    private Function getSequenceAnd(int card) {
        switch (card) {
            case Type.VALUE:
                return fnSequenceAndValue;
            case Type.OPTION:
                return fnSequenceAndOption;
            case Type.SEQUENCE:
                return fnSequenceAndSequence;
            default:
                throw new RuntimeException();
        }
    }

    //-----------------------------------------------------------------
    // static functions for transport attribution

    public static Object createOption() {
        return Option.TAG;
    }
    public static Object createSequence() {
        return new ArrayList();
    }

    public static Object createSequenceOption(Object obj) {
        List result;

        result = new ArrayList();
        if (obj != Option.TAG) {
            result.add(obj);
        }
        return result;
    }

    public static Object createSequenceValue(Object obj) {
        List result;

        result = new ArrayList();
        result.add(obj);
        return result;
    }

    /** Copy anything, option, value or sequence. */
    public static Object copy(Object obj) {
        return obj;
    }

    public static Object sequenceAndOption(Object list, Object obj) {
        if (obj != Option.TAG) {
            ((List) list).add(obj);
        }
        return list;
    }
    public static Object sequenceAndValue(Object list, Object obj) {
        ((List) list).add(obj);
        return list;
    }
    public static Object sequenceAndSequence(Object list, Object operand) {
        ((List) list).addAll((List) operand);
        return list;
    }
}
