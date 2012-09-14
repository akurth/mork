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
package net.oneandone.mork.compiler.tests.env;

import net.oneandone.mork.mapping.Mapper;

import java.io.StringReader;

/**
 * Test env arguments.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        Mapper mapper;
        Object[] result;

        mapper = new Mapper("net.oneandone.mork.compiler.tests.env.Mapper");
        mapper.setEnvironment(new Integer(3));
        result = mapper.run("<const>", new StringReader("ab"));
        System.out.println("result: " + result[0]);
    }

    public static int add(Object left, Object right) {
        return ((Integer) left).intValue() + ((Integer) right).intValue();
    }
}
