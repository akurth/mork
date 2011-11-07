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

package net.sf.beezle.mork.classfile;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import net.sf.beezle.sushi.fs.World;

import java.io.IOException;

public class RepositoryTest {
    private World world;
    private Repository repository;
    
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
