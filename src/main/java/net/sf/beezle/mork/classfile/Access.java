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

package net.sf.beezle.mork.classfile;

import java.util.HashSet;
import java.util.Set;

/**
 * ACC_ constants from Java Virtual Machine Specification.
 */
public enum Access {
    PUBLIC(0x0001),
    PRIVATE(0x0002),
    PROTECTED(0x0004),
    STATIC(0x0008),
    FINAL(0x0010),
    /** always set by new compilers */
    SUPER(0x0020, true),
    SYNCHRONIZED(0x0020),
    VOLATILE(0x0040),
    TRANSIENT(0x0080),
    NATIVE(0x0100),
    INTERFACE(0x0200, true),
    ABSTRACT(0x0400);

    private final int code;
    
    /** only for class access flags */
    private final boolean forClass;
    
    Access(int code) {
        this(code, false);
    }
    Access(int code, boolean forClass) {
        this.code = code;
        this.forClass = forClass;
    }
    
    public static Set<Access> fromFlags(char flags, boolean forClass) {
        Set<Access> result;
        
        result = new HashSet<Access>();
        for (Access a : values()) {
            if ((!a.forClass || forClass) && (flags & a.code) == a.code) {
                result.add(a);
                flags &= ~a.code;
            }
        }
        return result;
    }
    
    public static Set<Access> fromArray(Access ... flags) {
        return new HashSet<Access>(java.util.Arrays.asList(flags));
    }
    
    public static char toFlags(Set<Access> lst) {
        char flags;
        
        flags = 0;
        for (Access a : lst) {
            flags |= a.code;
        }
        return flags;
    }
    
    public static String toPrefix(Set<Access> accessFlags) {
        StringBuilder builder;
        boolean first;
        
        builder = new StringBuilder();
        first = true;
        for (Access a : accessFlags) {
            if (a == SUPER) {
                // skip
            } else {
                if (first) {
                    first = false;
                } else {
                    builder.append(' ');
                }
                builder.append(a.toString().toLowerCase());
            }
        }
        if (!first) {
            builder.append(' ');
        }
        return builder.toString();
    }
}