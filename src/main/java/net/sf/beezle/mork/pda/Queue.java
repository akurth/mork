package net.sf.beezle.mork.pda;

public class Queue {
    private final State[] states;

    private int putPtr = 0;      // circular indices
    private int takePtr = 0;

    private int free;
    private int used;

    private int waitingPuts = 0;    // counts of waiting threads
    private int waitingTakes = 0;

    private final Object putMonitor = new Object();
    private final Object takeMonitor = new Object();

    private boolean terminate = false;
    private final int threadCount;

    public Queue(int threadCount) throws IllegalArgumentException {
        states = new State[1000];
        free = states.length;
        used = 0;
        this.threadCount = threadCount;
    }

    public void put(State x) {
        synchronized(putMonitor) {
            while (free <= 0) {
                ++waitingPuts;
                try {
                    putMonitor.wait();
                } catch(InterruptedException ie) {
                    throw new IllegalStateException(ie);
                } finally {
                    --waitingPuts;
                }
            }
            --free;
            states[putPtr] = x;
            putPtr = (putPtr + 1) % states.length;
        }
        synchronized(takeMonitor) { // directly notify
            if (terminate) {
                throw new IllegalStateException();
            }
            ++used;
            if (waitingTakes > 0)
                takeMonitor.notify();
        }
    }

    public State take() throws InterruptedException {
        State old;

        synchronized(takeMonitor) {
            if (terminate) {
                throw new IllegalStateException();
            }
            while (used <= 0) {
                ++waitingTakes;
                if (waitingTakes == threadCount) {
                    if (waitingPuts > 0) {
                        throw new IllegalStateException();
                    }
                    terminate = true;
                    takeMonitor.notifyAll();
                    throw new InterruptedException();
                }
                try {
                    takeMonitor.wait();
                } catch(InterruptedException ie) {
                    throw new IllegalStateException(ie);
                } finally {
                    --waitingTakes;
                }
                if (terminate) {
                    throw new InterruptedException();
                }
            }
            --used;
            old = states[takePtr];
            states[takePtr] = null;
            takePtr = (takePtr + 1) % states.length;
        }
        synchronized(putMonitor) {
            ++free;
            if (waitingPuts > 0)
                putMonitor.notify();
        }
        return old;
    }

}