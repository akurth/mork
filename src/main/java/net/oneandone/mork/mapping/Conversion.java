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
package net.oneandone.mork.mapping;

import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.mork.reflect.Composition;
import net.oneandone.mork.reflect.Function;
import net.oneandone.mork.reflect.Option;
import net.oneandone.mork.reflect.Selection;
import net.oneandone.mork.semantics.Attribute;
import net.oneandone.mork.semantics.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert, wrap/unwrap, re-arrange.
 */
public class Conversion {
    public static final String ARGUMENT_TYPE_MISMATCH =
        "Argument type mismatch";
    public static final String AMBIGUOUS_CONSTRUCTOR_NAME =
        "Ambiguous constructor name";

    private static void throwIllegalCall(
        String kind, Selection selection, Definition def, List<Argument> args) throws GenericException {
        StringBuilder msg;
        int max;
        int i;

        msg = new StringBuilder();
        msg.append("for attribute ");
        msg.append(def.getName());
        msg.append(":\n");
        msg.append("  constructors:\n");
        max = selection.size();
        for (i = 0; i < max; i++) {
            msg.append("    " + selection.getFunction(i) + "\n");
        }
        msg.append("  arguments:\n");
        max = args.size();
        for (Argument arg : args) {
            msg.append("    " + arg.getAttribute().type + " " + arg.getSourcesString() + "\n");
        }
        throw new GenericException(kind, msg.toString());
    }

    public static Function find(Selection selection, Definition def, List<Argument> args, List<Attribute> outAttrs) throws GenericException {
        int i;
        int max;
        Function tmp;
        Function resultFn;
        List<Attribute> resultArgs;
        List<Attribute> tmpInArgs;
        List<Attribute> tmpOutArgs;

        resultFn = null;
        resultArgs = null;
        max = selection.size();
        for (i = 0; i < max; i++) {
            tmpInArgs = getAttributes(args);
            tmpOutArgs = new ArrayList<Attribute>();
            tmp = arrange(selection.getFunction(i), tmpInArgs, tmpOutArgs);
            if (tmp != null) {
                if (resultFn != null) {
                    throwIllegalCall(AMBIGUOUS_CONSTRUCTOR_NAME, selection, def, args);
                }
                resultFn = tmp;
                resultArgs = tmpOutArgs;
            }
        }
        if (resultFn == null) {
            throwIllegalCall(ARGUMENT_TYPE_MISMATCH, selection, def, args);
        }

        outAttrs.addAll(resultArgs);
        return resultFn;
    }

    public static List<Attribute> getAttributes(List<Argument> args) {
        List<Attribute> lst;

        lst = new ArrayList<Attribute>();
        for (Argument arg : args) {
            lst.add(arg.getAttribute());
        }
        return lst;
    }

    private static Function arrange(Function fn, List<Attribute> inArgs, List<Attribute> outArgs) {
        int i;
        int max;

        max = fn.getParameterTypes().length;
        for (i = 0; i < max; i++) {
            fn = arrangeArg(fn, i, inArgs, outArgs);
            if (fn == null) {
                return null;
            }
        }
        if (inArgs.size() == 0) {
            return fn;
        } else {
            return null;
        }
    }

    private static Function arrangeArg(Function fn, int idx, List<Attribute> inArgs, List<Attribute> outArgs) {
        Class<?> type;
        Attribute attr;

        type = fn.getParameterTypes()[idx];
        attr = removeAssignable(type, inArgs);
        if (attr == null) {
            return null;
        }
        outArgs.add(attr);
        if (type.isArray()) {
            // List -> <reference>[]
            //  or
            // List -> <primitive>[]
            return new Composition(fn, idx, new ToArray(type.getComponentType()));
        } else if (List.class.isAssignableFrom(type)) {
            // List -> List
            // no conversion required:
            return fn;
        } else {
            // <reference> -> <reference>
            // <reference> -> <primitive>
            if ((attr.type.card == Type.OPTION) || type.isPrimitive()) {
                // Option is used to unwrap into primitive arguments;
                // if not an OPTION, the optional test inside is never true.
                fn = new Option(fn, idx);
            } else {
                // no conversion
            }
            return fn;
        }
    }

    private static Attribute removeAssignable(Class<?> type, List<Attribute> inArgs) {
        int i;
        int max;
        Attribute current;

        max = inArgs.size();
        for (i = 0; i < max; i++) {
            current = inArgs.get(i);
            if (isAssignableFrom(type, current.type)) {
                inArgs.remove(i);
                return current;
            }
        }
        return null;
    }

    public static boolean hasFormalArgument(Selection sel, Type type) {
        int i;
        int max;

        if (sel == null) {
            // do nothing - internal constructor has no formal argumets
        } else {
            max = sel.size();
            for (i = 0; i < max; i++) {
                if (hasFormalArgument(sel.getFunction(i), type)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasFormalArgument(Function fn, Type type) {
        Class<?>[] formalTypes;
        int i;

        formalTypes = fn.getParameterTypes();
        for (i = 0; i < formalTypes.length; i++) {
            if (isAssignableFrom(formalTypes[i], type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFrom(Class<?> formalType, Type actual) {
        Class<?> compType;

        if (actual.card == Type.SEQUENCE) {
            if (List.class.isAssignableFrom(formalType)) {
                return true;
            }
            if (!formalType.isArray()) {
                return false;
            }
            if (formalType.isAssignableFrom(actual.type)) {
                return true;
            }
            compType = formalType.getComponentType();
        } else {
            compType = formalType;
        }
        if (compType.isAssignableFrom(actual.type)) {
            return true;
        }
        if (compType.isPrimitive()) {
            if (ClassRef.wrappedType(compType).isAssignableFrom(actual.type)) {
                return true;
            }
        }
        return false;
    }
}
