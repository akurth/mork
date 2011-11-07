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

package de.mlhartme.mork.semantics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.mlhartme.mork.misc.GenericException;
import net.sf.beezle.sushi.graph.Graph;
import net.sf.beezle.sushi.graph.EdgeIterator;

/**
 * Helper class for OagBuilder
 */
public class Partition {
    public static List[] createA(Set synthesized, Set inherited, Graph idsX) throws GenericException {
        Graph closure;
        List done;
        List partitions;
        List[] result;
        int all;
        int initialSize;

        closure = new Graph();
        closure.addGraph(idsX);

        partitions = new ArrayList();
        done = new ArrayList();
        all = inherited.size() + synthesized.size();
        while (done.size() < all) {
            initialSize = done.size();
            partitions.add(extractStep(synthesized, done, closure));
            if (done.size() == all) {
                break;
            }
            partitions.add(extractStep(inherited, done, closure));
            if (initialSize == done.size()) {
                throw new GenericException("cyclic dependency");
            }
        }
        result = new List[partitions.size()];
        partitions.toArray(result);
        return result;
    }

    private static List extractStep(Collection lefts, Collection rights, Graph relation) {
        List current;
        List all;

        all = new ArrayList();
        while (true) { // loop is required because dependencies into the same parition are allowed
            current = getDisconnected(lefts, relation, rights);
            if (current.size() == 0) {
                return all;
            }
            lefts.removeAll(current);
            rights.addAll(current);
            all.addAll(current);
        }
    }

    /**
     * Returns all objects from leftCollection whole images are a disjoin from rightCollection,
     * none of the resulting objects has an image in rightCollection. Compares objects using ==.
     * Note that lefts with no image show up in the result.
     */
    public static List getDisconnected(
        Collection leftCollection, Graph relation, Collection rightCollection)
    {
        List disconnected;
        Iterator iter;
        Object left;
        EdgeIterator relationIter;

        disconnected = new ArrayList();
        iter = leftCollection.iterator();
        while (iter.hasNext()) {
            left = iter.next();
            relationIter = relation.edges();
            while (relationIter.step()) {
                if (relationIter.left() == left) {
                    if (!rightCollection.contains(relationIter.right())) {
                        relationIter = null;
                        break;
                    }
                }
            }
            if (relationIter != null) {
                disconnected.add(left);
            }
        }
        return disconnected;
    }
}
