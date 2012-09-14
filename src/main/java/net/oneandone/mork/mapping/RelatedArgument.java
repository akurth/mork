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
package net.oneandone.mork.mapping;

import net.oneandone.mork.semantics.Compare;

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
     */
    public static List<List<Argument>> sort(List<Argument> args) {
        int i;
        int max;
        List<List<Argument>> result;
        List<RelatedArgument> remaining;    // List of RelatedArguments
        List<RelatedArgument> heads;   // List of RelatedArguments

        result = new ArrayList<List<Argument>>();
        remaining = createRelatedArgs(args);
        while (true) {
            max = remaining.size();
            if (max == 0) {
                return result;
            }
            heads = new ArrayList<RelatedArgument>();
            for (i = 0; i < max; i++) {
                addHead(heads, remaining.get(i));
            }
            remaining.clear();
            result.add(separate(heads, remaining));
        }
    }

    /**
     * @param args  List of Arguments
     * @return List of RelatedArguments
     */
    private static List<RelatedArgument> createRelatedArgs(List<Argument> args) {
        List<RelatedArgument> related;
        int i;
        int max;

        related = new ArrayList<RelatedArgument>();
        max = args.size();
        for (i = 0; i < max; i++) {
            related.add(new RelatedArgument(args.get(i)));
        }
        return related;
    }

    /**
     * @param heads   List of RelatedArguments
     */
    private static void addHead(List<RelatedArgument> heads, RelatedArgument left) {
        int i;
        int max;
        RelatedArgument right;
        boolean foundLT;
        boolean foundGT;

        max = heads.size();
        foundLT = false;
        foundGT = false;
        for (i = max - 1; i >= 0; i--) {
            right = heads.get(i);
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
    private static List<Argument> separate(List<RelatedArgument> heads, List<RelatedArgument> tails) {
        List<Argument> merge;

        merge = new ArrayList<Argument>();
        for (RelatedArgument head : heads) {
            merge.add(head.arg);
            tails.addAll(head.nexts);
        }
        return merge;
    }


    private final Argument arg;

    private final List<RelatedArgument> nexts;

    public RelatedArgument(Argument arg) {
        this.arg = arg;
        this.nexts = new ArrayList<RelatedArgument>();
    }
}
