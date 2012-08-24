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
package net.sf.beezle.mork.reflect;

import net.sf.beezle.mork.classfile.ClassRef;
import net.sf.beezle.mork.classfile.Code;

import java.lang.reflect.InvocationTargetException;

/**
  * Composition of two functions. Invokation first invokes the
  * so-called parameter Function. The result is used for the
  * so-called computed argument to the so-called base Function.
  * The arguments of the Composition is the argument list of the
  * base Function with the computed argument sustituted by the
  * arguments of the parameter Function.
  */

public class Composition extends Function {
    /** Base Function. The main function to be invoked */
    private final Function base;

    /** Parameter Function. It computes an argument for base */
    private final Function para;

    /** Computed argument. Which argument of base is computed by para */
    private final int idx;

    private final int baseParaCount;
    private final int paraParaCount;

    /**
     * Create a Composition.
     * @param baseInit  base Function for this Composition
     * @param idxInit   computed argument for this Composition
     * @param paraInit  parameter Function for this Composition
     */
    public Composition(Function baseInit, int idxInit, Function paraInit) {
        base = baseInit;
        idx = idxInit;
        para = paraInit;
        baseParaCount = base.getParameterTypes().length;
        paraParaCount = para.getParameterTypes().length;
    }

    public static Composition create(Function baseInit, int idxInit, Function paraInit) {
        Class[] tmp;

        tmp = baseInit.getParameterTypes();
        if (idxInit >= tmp.length) {
            return null; // "invalid index"
        }
        if (!tmp[idxInit].isAssignableFrom(paraInit.getReturnType())) {
            return null; // "type mismatch"
        }

        return new Composition(baseInit, idxInit, paraInit);
    }

    /**
     * Gets the Function name.
     * @return the Function name
     */
    @Override
    public String getName() {
        return base.getName() + idx + para.getName();
    }

    /**
     * Gets the result type. The result type is the result type of the
     * base Function.
     * @return the result type
     */
    @Override
    public Class getReturnType() {
        return base.getReturnType();
    }

    /**
     * Gets the argument count. The parameter function takes away 1
     * argument from the base function, but it adds its own arguments.
     * @return  the argument count
     */
    @Override
    public Class[] getParameterTypes() {
        Class[] a, b, result;

        a = base.getParameterTypes();
        b = para.getParameterTypes();
        result = new Class[a.length - 1 + b.length];
        System.arraycopy(a, 0,  result, 0,  idx);
        System.arraycopy(b, 0,  result, idx, b.length);
        System.arraycopy(a, idx + 1,  result, idx + b.length,
                         a.length - (idx + 1));
        return result;
    }

    @Override
    public Class[] getExceptionTypes() {
        return (Class[]) Arrays.append(Class.class,
                                       base.getExceptionTypes(),
                                       para.getExceptionTypes());
    }

    /**
     * Invokes the composed Functions. The computed argument
     * is computed by a call to the parameter Function.
     * @return  the result returned by the base Function.
     */
    @Override
    public Object invoke(Object[] allVals) throws InvocationTargetException {
        Object[] vals;
        Object tmp;

        vals = new Object[paraParaCount];
        System.arraycopy(allVals, idx, vals, 0, vals.length);
        tmp = para.invoke(vals);

        vals = new Object[baseParaCount];
        System.arraycopy(allVals, 0, vals, 0, idx);
        vals[idx] = tmp;
        System.arraycopy(allVals, idx + paraParaCount, vals, idx + 1, vals.length - (idx + 1));

        return base.invoke(vals);
    }

    @Override
    public void translate(Code dest) {
        int max;
        int ofs;
        int var;
        int[] vars;
        ClassRef type;
        ClassRef[] types;
        Class[] tmp;
        int i;

        tmp = getParameterTypes();
        ofs = idx + paraParaCount;
        max = tmp.length - ofs;
        vars = new int[max];
        types = new ClassRef[max];
        for (i = max - 1; i >= 0; i--) {
            type = new ClassRef(tmp[i + ofs]);
            types[i] = type;
            var = dest.allocate(type);
            vars[i] = var;
            type.emitStore(dest, var);
        }
        para.translate(dest);
        for (i = 0; i < max; i++) {
            types[i].emitLoad(dest, vars[i]);
        }
        base.translate(dest);
    }
}
