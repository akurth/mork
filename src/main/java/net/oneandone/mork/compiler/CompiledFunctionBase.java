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
package net.oneandone.mork.compiler;

import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.reflect.Function;

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
