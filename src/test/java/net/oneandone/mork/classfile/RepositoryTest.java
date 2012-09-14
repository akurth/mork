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
package net.oneandone.mork.classfile;

import net.oneandone.sushi.fs.World;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RepositoryTest {
    private final World world;
    private final Repository repository;

    public RepositoryTest() throws IOException {
        world = new World();
        repository = new Repository();
        repository.addAllLazy(world.locateClasspathItem(Object.class));
    }

    @Test
    public void resolvePrimitive() throws Exception {
        assertNull(repository.lookup("byte"));
    }

    @Test
    public void resolveInterfaceMethod() throws Exception {
        ClassDef oo;
        MethodRef m;

        oo = repository.lookup("java.io.ObjectOutput");
        assertNotNull(oo);
        m = new MethodRef(oo.reference(), false, ClassRef.VOID, "writeUTF", ClassRef.STRING);
        assertNotNull(m.resolve(repository));
    }
}
