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

package net.sf.beezle.mork.semantics;

import junit.framework.TestCase;
import net.sf.beezle.mork.misc.GenericException;
import net.sf.beezle.sushi.graph.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PartitionTest extends TestCase {
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";
    private static final String E = "E";

    private List left;
    private List right;
    private Set inherited;
    private Set synthesized;
    private Graph relation;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        left = new ArrayList();
        right = new ArrayList();
        inherited = new HashSet();
        synthesized = new HashSet();
        relation = new Graph();
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
