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
package net.oneandone.mork.classfile;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PoolTest {
    @Test
    public void utf8() {
        check("");
        check("abc");
        check("hello, world!");
        for (char c = 0; c < 512; c++) {
            check("" + c);
            check("123" + c + "456");
        }
        for (char c = 517; c < 20048; c+=47) {
            check("" + c);
            check("xyz" + c + "XYZ");
        }
    }

    private void check(String str) {
        byte[] utf8;

        utf8 = Pool.toUtf8(str);
        assertEquals(str, Pool.fromUtf8(utf8, 3, utf8.length));
    }
}
