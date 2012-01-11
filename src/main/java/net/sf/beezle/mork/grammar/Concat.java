package net.sf.beezle.mork.grammar;

public class Concat {
    private final PrefixSet done;
    private PrefixSet todo;

    public Concat(int k) {
        done = new PrefixSet(k);
        todo = new PrefixSet(k);
        todo.add(Prefix.EMPTY);
    }

    /** true when done */
    public boolean with(PrefixSet op) {
        PrefixSet next;
        Prefix tmp;

        next = new PrefixSet(done.k);
        for (Prefix l : todo) {
            for (Prefix r : op) {
                tmp = l.concat(r, done.k);
                if (tmp.size() == done.k) {
                    done.add(tmp);
                } else {
                    next.add(tmp);
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
