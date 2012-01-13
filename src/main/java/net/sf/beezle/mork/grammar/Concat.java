package net.sf.beezle.mork.grammar;

public class Concat {
    private final int k;
    private final PrefixSet done;
    private PrefixSet todo;

    public Concat(int k) {
        this.k = k;
        this.done = new PrefixSet();
        this.todo = PrefixSet.zero();
    }

    /** true when done */
    public boolean with(PrefixSet op) {
        PrefixSet next;
        Prefix tmp;

        next = new PrefixSet();
        for (Prefix l : todo) {
            for (Prefix r : op) {
                tmp = l.concat(r, k);
                if (tmp.size() == k) {
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
