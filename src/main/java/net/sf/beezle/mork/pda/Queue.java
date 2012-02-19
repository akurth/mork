package net.sf.beezle.mork.pda;

public class Queue {
    private static class Element {
        public final State state;
        public final Element next;
        
        public Element(State state, Element next) {
            this.state = state;
            this.next = next;
        }
    }

    private final int threadCount;
    private Element states;
    private final Object monitor;
    private int waitingTakes;
    private boolean terminate;

    public Queue(int threadCount) throws IllegalArgumentException {
        this.threadCount = threadCount;
        this.states = null;
        this.monitor = new Object();
        this.waitingTakes = 0;
        this.terminate = false;
    }

    public void put(State x) {
        synchronized (monitor) {
            if (terminate) {
                throw new IllegalStateException();
            }
            states = new Element(x, states);
            monitor.notifyAll();
        }
    }

    public State take() throws InterruptedException {
        State result;

        synchronized (monitor) {
            if (terminate) {
                throw new IllegalStateException();
            }
            while (states == null) {
                ++waitingTakes;
                if (waitingTakes == threadCount) {
                    terminate = true;
                    monitor.notifyAll();
                    throw new InterruptedException();
                }
                try {
                    monitor.wait();
                } catch (InterruptedException ie) {
                    throw new IllegalStateException(ie);
                } finally {
                    --waitingTakes;
                }
                if (terminate) {
                    throw new InterruptedException();
                }
            }
            result = states.state;
            states = states.next;
        }
        return result;
    }
}