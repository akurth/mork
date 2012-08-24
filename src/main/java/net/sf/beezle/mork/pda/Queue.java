/**
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
            monitor.notify();
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