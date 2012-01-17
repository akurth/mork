package net.sf.beezle.mork.grammar;

public class Concat {
    private final int k;
    private final long firstFullValue;
    private final PrefixSet done;
    private PrefixSet todo;

    public Concat(int k) {
        this.k = k;
        this.firstFullValue = exp(Prefix.BASE, k - 1);
        this.done = new PrefixSet();
        this.todo = PrefixSet.one();
    }

    private static long exp(int x, int y) {
        long result;

        result = 1;
        for (int i = 1; i <= y; i++) {
            result *= x;
        }
        return result;
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
                if (tmp >= firstFullValue) {
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
