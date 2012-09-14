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
