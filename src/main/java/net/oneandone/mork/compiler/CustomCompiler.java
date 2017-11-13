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

public abstract class CustomCompiler {
    public abstract boolean matches(Class<?> type);

    /**
     * The custom compile is allowed to generate at most MIN_INSTRUCTIONS
     */
    public abstract void beginTranslation(Object obj, Code dest);

    /**
     * The custom compiler is allowed to generate at most MIN_INSTRUCTUINS.
     */
    public abstract void endTranslation(Object obj, Code dest);

    public abstract Class<?>[] getFieldTypes();
    public abstract Object[] getFieldObjects(Object obj);
}
