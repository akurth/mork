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
        long tmp;
        PrefixIterator todoIter;
        Prefix l;
        PrefixIterator opIter;
        Prefix r;

        next = new PrefixSet();
        todoIter = todo.iterator();
        while (todoIter.hasNext()) {
            l = todoIter.next();
            opIter = op.iterator();
            while (opIter.hasNext()) {
                r = opIter.next();
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
