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

package net.sf.beezle.mork.mapping;

import net.sf.beezle.mork.semantics.Compare;

import java.util.ArrayList;
import java.util.List;

/**
 * To sort arguments.
 */
public class RelatedArgument implements Compare {
    /**
     * Returns the sorted list of lists of arguments.
     *
     * @param args   normal argument, not related arguments
     * @return List of List of Arguments.
     */
    public static List<Object> sort(List<Object> args) {
        int i;
        int max;
        List result;  // List of Arguments
        List remaining;    // List of RelatedArguments
        List heads;   // List of RelatedArguments

        result = new ArrayList();
        remaining = createRelatedArgs(args);
        while (true) {
            max = remaining.size();
            if (max == 0) {
                return result;
            }
            heads = new ArrayList();
            for (i = 0; i < max; i++) {
                addHead(heads, (RelatedArgument) remaining.get(i));
            }
            remaining.clear();
            result.add(separate(heads, remaining));
        }
    }

    /**
     * @param args  List of Arguments
     * @return List of RelatedArguments
     */
    private static List createRelatedArgs(List args) {
        List related;
        int i;
        int max;

        related = new ArrayList();
        max = args.size();
        for (i = 0; i < max; i++) {
            related.add(new RelatedArgument((Argument) args.get(i)));
        }
        return related;
    }

    /**
     * @param heads   List of RelatedArguments
     */
    private static void addHead(List heads, RelatedArgument left) {
        int i;
        int max;
        int initialMax;
        RelatedArgument right;
        boolean foundLT;
        boolean foundGT;

        max = heads.size();
        foundLT = false;
        foundGT = false;
        for (i = max - 1; i >= 0; i--) {
            right = (RelatedArgument) heads.get(i);
            switch (left.arg.compare(right.arg)) {
                case LT:
                    left.nexts.add(right);
                    heads.remove(i);
                    foundLT = true;
                    break;
                case GT:
                    if (!foundGT) {
                        right.nexts.add(left);
                        foundGT = true;
                    } else {
                        // ignore - left must not be duplicated in right.nexts
                    }
                    break;
                default:
                    // do nothing
                    break;
            }
        }
        if (foundGT && foundLT) {
            throw new IllegalStateException();
        }
        if (!foundGT) {
            heads.add(left);
        }
    }

    /**
     ** @param heads  List of RelatedArguments
     ** @param tails out-argument, List of RelatedArguments
     ** @return List of arguments
     **/
    private static List separate(List heads, List tails) {
        int i;
        int max;
        List merge;
        RelatedArgument head;

        max = heads.size();
        merge = new ArrayList();
        for (i = 0; i < max; i++) {
            head = (RelatedArgument) heads.get(i);
            merge.add(head.arg);
            tails.addAll(head.nexts);
        }
        return merge;
    }


    private final Argument arg;

    // list of ReleatedArguments
    private final List nexts;

    public RelatedArgument(Argument arg) {
        this.arg = arg;
        this.nexts = new ArrayList();
    }
}
