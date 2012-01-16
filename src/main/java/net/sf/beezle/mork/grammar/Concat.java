package net.sf.beezle.mork.grammar;

public class Concat {
    private final int k;
    private final PrefixSet done;
    private PrefixSet todo;

    public Concat(int k) {
        this.k = k;
        this.done = new PrefixSet();
        this.todo = PrefixSet.one();
    }

    /** true when done */
    public boolean with(PrefixSet op) {
        PrefixSet next;
        long tmp;
        Prefix l;
        Prefix r;

        next = new PrefixSet();
        l = todo.iterator();
        while (l.step()) {
            r = op.iterator();
            while (r.step()) {
                tmp = Prefix.concat(l.data, r.data, k);
                if (Prefix.size(tmp) == k) {
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
