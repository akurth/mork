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
package net.oneandone.mork.compiler;

import net.oneandone.mork.classfile.Bytecodes;
import net.oneandone.mork.classfile.ClassRef;
import net.oneandone.mork.classfile.Code;
import net.oneandone.mork.classfile.MethodRef;

public class Util implements Bytecodes {
    public static void unwrap(Class<?> cl, Code dest) {
        ClassRef wrapper;

        if (cl.isPrimitive()) {
            wrapper = new ClassRef(ClassRef.wrappedType(cl));
            dest.emit(CHECKCAST, wrapper);
            dest.emit(INVOKEVIRTUAL,
                      MethodRef.meth(wrapper, new ClassRef(cl), cl.getName() + "Value"));
        } else {
            // do nothing
            //  dest.emit(CHECKCAST, new ClassRef(cl));
        }
    }
}
