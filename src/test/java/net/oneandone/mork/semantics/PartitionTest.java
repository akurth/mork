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
package net.oneandone.mork.semantics;

import junit.framework.TestCase;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.sushi.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PartitionTest extends TestCase {
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";

    private List<String> left;
    private List<String> right;
    private Set<String> inherited;
    private Set<String> synthesized;
    private Graph<String> relation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        left = new ArrayList<String>();
        right = new ArrayList<String>();
        inherited = new HashSet<String>();
        synthesized = new HashSet<String>();
        relation = new Graph<String>();
    }

    //--- disconnected

    public void testSimpleDisconnection() {
        List disconnected;

        left.add(A);
        left.add(B);
        right.add(C);
        right.add(D);
        relation.addEdge(A, C);
        relation.addEdge(B, D);
        disconnected = Partition.getDisconnected(left, relation, right);
        assertEquals(2, disconnected.size());
        assertSame(A, disconnected.get(0));
        assertSame(B, disconnected.get(1));
    }

    public void testTransitiveDisconnection() {
        List connected;

        left.add(A);
        right.add(C);
        relation.addEdge(A, B);
        relation.addEdge(B, C);
        connected = Partition.getDisconnected(left, relation, right);
        assertEquals(0, connected.size());
    }

    public void testWithoutDisconnections() {
        List disconnected;

        left.add(A);
        left.add(D);
        right.add(B);
        // relation is empty
        disconnected = Partition.getDisconnected(left, relation, right);
        assertEquals(2, disconnected.size());
        assertSame(A, disconnected.get(0));
        assertSame(D, disconnected.get(1));
    }

    public void testDisconnected() {
        List disconnected;

        left.add(A);
        left.add(D);
        right.add(B);
        relation.addEdge(A, B);
        relation.addEdge(A, C);
        relation.addEdge(B, D);
        relation.addEdge(D, D);
        disconnected = Partition.getDisconnected(left, relation, right);
        assertEquals(0, disconnected.size());
    }

    //--- createA

    public void testEmpty() throws GenericException {
        List[] partitions;

        partitions = Partition.createA(synthesized, inherited, relation);
        assertEquals(0, partitions.length);
    }

    public void testSynthesizedOnly() throws GenericException {
        List[] partitions;

        synthesized.add(A);
        partitions = Partition.createA(synthesized, inherited, relation);
        assertEquals(1, partitions.length);
        assertEquals(1, partitions[0].size());
        assertSame(A, partitions[0].get(0));
    }

    public void testInheritedOnly() throws GenericException {
        List[] partitions;

        inherited.add(A);
        partitions = Partition.createA(synthesized, inherited, relation);
        assertEquals(2, partitions.length);
        assertEquals(0, partitions[0].size());
        assertEquals(1, partitions[1].size());
        assertSame(A, partitions[1].get(0));
    }

    public void testInheritedBeforeSynthesized() throws GenericException {
        List[] partitions;

        synthesized.add(A);
        inherited.add(B);
        relation.addEdge(A, B);
        partitions = Partition.createA(synthesized, inherited, relation);
        assertEquals(3, partitions.length);
        assertEquals(0, partitions[0].size());
        assertEquals(1, partitions[1].size());
        assertSame(B, partitions[1].get(0));
        assertEquals(1, partitions[2].size());
        assertSame(A, partitions[2].get(0));
    }

    public void testTwoInOnePartition() throws GenericException {
        List[] partitions;

        synthesized.add(A);
        synthesized.add(B);
        relation.addEdge(A, B);
        inherited.add(C);
        inherited.add(D);
        relation.addEdge(D, C);
        partitions = Partition.createA(synthesized, inherited, relation);
        assertEquals(2, partitions.length);
        assertTrue(partitions[0].contains(A));
        assertTrue(partitions[0].contains(B));
        assertEquals(2, partitions[1].size());
        assertTrue(partitions[1].contains(C));
        assertTrue(partitions[1].contains(D));
    }
}
