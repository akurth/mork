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
package net.oneandone.mork.semantics;

import net.oneandone.mork.grammar.Grammar;
import net.oneandone.mork.misc.StringArrayList;
import net.oneandone.mork.reflect.Function;
import net.oneandone.mork.scanner.Position;

import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Attribute function call.
 */
public class Attribution implements Serializable {
    private final Function function;

    // ofs -1: left hand side

    private final int resultOfs;
    private final int resultAttr;

    private final int[] argsOfs;
    private final int[] argsAttr;

    private final transient Object[] args;

    /**
     * Nonterminal attribution.
     * ofs = -1 for left hand side.
     */
    public Attribution(
        Function function, int resultOfs, int resultAttr, int[] argsOfs, int[] argsAttr)
    {
        this.function = function;
        this.resultOfs = resultOfs;
        this.resultAttr = resultAttr;
        this.argsOfs = argsOfs;
        this.argsAttr = argsAttr;
        this.args = new Object[argsOfs.length];
    }

    /**
     * Caution: do not call this method concurrently!
     *
     * @param ctx  left-hand-side node
     */
    public void eval(Node ctx, PrintWriter verbose) throws SemanticError {
        Object result;
        int i;
        Throwable t;
        Position pos;

        for (i = 0; i < args.length; i++) {
            args[i] = ctx.get(argsOfs[i]).attrs[argsAttr[i]];
        }
        try {
            result = function.invoke(args);
        } catch (InvocationTargetException e) {
            t = e.getTargetException();
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            if (t instanceof Exception) {
                if (verbose != null) {
                    log("[FAILURE]", verbose);
                }
                pos = new Position();
                pos.set(ctx.position);
                throw new SemanticError(pos, (Exception) t);
            }
            throw new RuntimeException("illegal exception type: " + t);
        }
        ctx.get(resultOfs).attrs[resultAttr] = result;
        if (verbose != null) {
            log(result, verbose);
        }
    }

    /**
     * @param verbose != null
     */
    private void log(Object result, PrintWriter verbose) {
        int i;

        verbose.print("attribution: ");
        verbose.print(shortName(result));
        verbose.print(" <- ");
        verbose.print(function.getName());
        verbose.print('(');
        for (i = 0; i < args.length; i++) {
            if (i > 0) {
                verbose.print(", ");
            }
            verbose.print(shortName(args[i]));
        }
        verbose.println(")");
    }

    private static String shortName(Object obj) {
        String clazz;
        int idx;

        if (obj == null) {
            return "null";
        } else {
            clazz = obj.getClass().getName();
            idx = clazz.lastIndexOf('.');
            clazz = clazz.substring(idx + 1); // ok for idx == -1
            return clazz + "#" + Integer.toHexString(obj.hashCode());
        }
    }

    @Override
    public String toString() {
        int i;
        StringBuilder buf;

        buf = new StringBuilder();
        buf.append(function.toString());
        buf.append(resultAttr);
        buf.append('$');
        buf.append(resultOfs);
        buf.append('(');
        for (i = 0; i < argsOfs.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(argsAttr[i] + "$" + argsOfs[i]);
        }
        buf.append(')');
        return buf.toString();
    }

    public String toString(StringArrayList symTab, Grammar grm) {
        StringBuilder buf;
        int i;

        buf = new StringBuilder();
        buf.append('\t');
        buf.append("" + function);
        buf.append("\n\t\t");
        buf.append(toStringRef(resultOfs, resultAttr));
        buf.append("\t<- ");
        for (i = 0; i < argsOfs.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(toStringRef(argsOfs[i], argsAttr[i]));
        }
        buf.append('\n');
        return buf.toString();
    }

    private static String toStringRef(int ofs, int attr) {
        return "($" + (ofs + 1) + ")." + attr;
    }
}
