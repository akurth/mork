package net.sf.beezle.mork.grammar;

import net.sf.beezle.sushi.util.IntArrayList;

public class Concat {
    private final PrefixSet done;
    private PrefixSet todo;
    
    public Concat(int k) {
        done = new PrefixSet(k);
        todo = new PrefixSet(k);
        todo.add(new IntArrayList());
    }

    /** true when done */
    public boolean with(PrefixSet op) {
        PrefixSet next;
        IntArrayList array;

        next = new PrefixSet(done.k);
        for (IntArrayList l : todo) {
            for (IntArrayList r : op) {
                array = concat(l, r, done.k);
                if (array.size() == done.k) {
                    done.add(array);
                } else {
                    next.add(array);
                }
            }
        }
        todo = next;
        return todo.isEmpty();
    }
    
    public PrefixSet result() {
        done.addAll(todo);
        return done;
    }

    public static IntArrayList concat(IntArrayList left, IntArrayList right, int k) {
        IntArrayList result;

        if (left.size() > k) {
            throw new IllegalArgumentException();
        }
        result = new IntArrayList(left);
        for (int i = 0; i < right.size(); i++) {
            if (result.size() >= k) {
                break;
            }
            result.add(right.get(i));
        }
        return result;
    }
}
