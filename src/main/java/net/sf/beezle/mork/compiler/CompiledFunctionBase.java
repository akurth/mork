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

package net.sf.beezle.mork.compiler;

import net.sf.beezle.mork.classfile.Code;
import net.sf.beezle.mork.reflect.Function;

public abstract class CompiledFunctionBase extends Function {
    @Override
    public String getName() {
        return "fn" + hashCode();
    }
    @Override
    public Class<?> getReturnType() {
        throw new UnsupportedOperationException();
    }
    @Override
    public Class<?>[] getParameterTypes() {
        throw new UnsupportedOperationException();
    }
    @Override
    public Class<?>[] getExceptionTypes() {
        throw new UnsupportedOperationException();
    }

    // commenting in
    //    public abstract Object invoke(Object[] vals);
    // causes javap (sun jdk 1.3.1 and 1.4 beta 2) to fail with a NullPointerException
    // when called for a class derived from this class!?

    @Override
    public void translate(Code dest) {
        throw new UnsupportedOperationException();
    }
}
