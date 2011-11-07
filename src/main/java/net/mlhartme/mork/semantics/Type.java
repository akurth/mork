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

package de.mlhartme.mork.semantics;

import de.mlhartme.mork.classfile.ClassRef;

/**
 * Type of an attribute. Immutable, can safely be shared.
 */

public class Type {
    // Cardinality. Decodes priorities when alternating variables
    public static final int VALUE = 0;
    public static final int OPTION = 1;
    public static final int SEQUENCE = 2;

    //----------------------------------------------------------------

    public final Class type; // non-primitive types only
    public final int card;

    //-----------------------------------------------------------------

    public Type(Class type) {
        this(type, VALUE);
    }

    public Type(Class type, int card) {
        if (type.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.card = card;
    }

    public static int cardCard(int first, int second) {
        if (first == SEQUENCE || second == SEQUENCE) {
            return SEQUENCE;
        }
        if (first == OPTION || second == OPTION) {
            return OPTION;
        }
        return VALUE;
    }

    @Override
    public boolean equals(Object obj) {
        Type operand;

        if (obj instanceof Type) {
            operand = (Type) obj;
            return
                (card == operand.card) && (type.equals(operand.type));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    public Type option() {
        if (card >= OPTION) {
            return this;
        } else {
            return new Type(type, OPTION);
        }
    }

    public Type sequence() {
        return new Type(type, SEQUENCE);
    }

    public Type alternate(Type operand) {
        Class tmp;

        tmp = ClassRef.commonBase(type, operand.type);
        return new Type(tmp, Math.max(card, operand.card));
    }

    @Override
    public String toString() {
        return type.getName() + cardString();
    }

    public String cardString() {
        switch (card) {
        case VALUE:
            return "";
        case OPTION:
            return "?";
        case SEQUENCE:
            return "*";
        default:
            throw new RuntimeException();
        }
    }
}
