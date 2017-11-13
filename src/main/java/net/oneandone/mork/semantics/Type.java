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
package net.oneandone.mork.semantics;

import net.oneandone.mork.classfile.ClassRef;

/**
 * Type of an attribute. Immutable, can safely be shared.
 */

public class Type {
    // Cardinality. Decodes priorities when alternating variables
    public static final int VALUE = 0;
    public static final int OPTION = 1;
    public static final int SEQUENCE = 2;

    //--

    public final Class type; // non-primitive types only
    public final int card;

    //--

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
