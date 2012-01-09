package net.sf.beezle.mork.grammar;

import net.sf.beezle.sushi.util.IntArrayList;

public class Concat {
    private final PrefixSet done;
    private PrefixSet todo;

    public Concat(int k) {
        done = new PrefixSet(k);
        todo = new PrefixSet(k);
        todo.add(new Prefix());
    }

    /** true when done */
    public boolean with(PrefixSet op) {
        PrefixSet next;
        Prefix array;

        next = new PrefixSet(done.k);
        for (Prefix l : todo) {
            for (Prefix r : op) {
                array = l.concat(r, done.k);
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
}
