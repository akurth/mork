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

import net.oneandone.mork.misc.GenericException;
import net.oneandone.sushi.graph.EdgeIterator;
import net.oneandone.sushi.graph.Graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Helper class for OagBuilder
 */
public class Partition {
    public static List<Attribute>[] createA(Set synthesized, Set inherited, Graph idsX) throws GenericException {
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
