/*
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

package net.sf.beezle.mork.classfile;

import net.sf.beezle.sushi.util.IntArrayList;
import net.sf.beezle.sushi.util.IntBitSet;
import net.sf.beezle.sushi.util.IntCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * A jsr/ret subroutine. Used to compute the instructions that can
 * follow a ret instruction.
 */

public class Jsr implements Bytecodes {
    /**
     * Start idx. Never a label.
     */
    public final int start;

    /**
     * Caller indexes. Never a label
     */
    public final IntArrayList caller;

    /**
     * Return indexes. Never a label
     */
    public final IntArrayList rets;

    public Jsr(int startInit, Code code) {
        if (startInit < 0) {
            throw new IllegalArgumentException("start < 0: " + startInit);
        }

        start = startInit;
        caller = new IntArrayList();
        rets = new IntArrayList();
        calcRets(code);
    }

    private void calcRets(Code code) {
        IntBitSet todo;
        IntBitSet reached;
        int idx;
        Instruction instr;
        List<Jsr> empty;

        empty = new ArrayList<Jsr>();
        todo = new IntBitSet();
        todo.add(start);
        reached = new IntBitSet();
        while (true) {
            idx = todo.first();
            if (idx == -1) {
                break;
            }
            todo.remove(idx);
            if (!reached.contains(idx)) {
                reached.add(idx);
                instr = code.instructions.get(idx);
                instr.getSuccessors(empty, idx, code, todo);
            }
        }
        for (idx = reached.first(); idx != -1; idx = reached.next(idx)) {
            instr = code.instructions.get(idx);
            if (instr.type.opcode == RET) {
                rets.add(idx);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result;

        result = new StringBuilder();
        result.append("jsr " + start);
        result.append(" rets: " + rets);
        result.append(" caller: " + caller);
        return result.toString();
    }

    //-----------------------------------------------------------------

    public static List<Jsr> findJsrs(Code code) {
        List<Jsr> result;
        int idx;
        int max;
        Instruction instr;
        Jsr jsr;
        int start;

        result = new ArrayList<Jsr>();
        max = code.instructions.size();
        for (idx = 0; idx < max; idx++) {
            instr = (Instruction) code.instructions.get(idx);
            if (instr.type.opcode == JSR) {
                start = code.resolveLabel(((Integer)
                                           instr.arguments[0]).intValue());
                jsr = findJsr(result, start);
                if (jsr == null) {
                    jsr = new Jsr(start, code);
                    result.add(jsr);
                }
                jsr.caller.add(idx);
            }
        }
        return result;
    }

    public static Jsr findJsr(List<Jsr> jsrs, int st) {
        Jsr result;
        int i, max;

        max = jsrs.size();
        for (i = 0; i < max; i++) {
            result = (Jsr) jsrs.get(i);
            if (result.start == st) {
                return result;
            }
        }
        return null;
    }

    /**
     * idx  index of ret
     */
    public static void addRetSuccessors(List<Jsr> jsrs, int idx, IntCollection result) {
        int i, max;

        max = jsrs.size();
        for (i = 0; i < max; i++) {
            ((Jsr) jsrs.get(i)).addRetSuccessors(idx, result);
        }
    }

    public void addRetSuccessors(int idx, IntCollection result) {
        int i, max;

        if (rets.indexOf(idx) != -1) {
            max = caller.size();
            for (i = 0; i < max; i++) {
                result.add(caller.get(i) + 1);
            }
        }
    }
}
