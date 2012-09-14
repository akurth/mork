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
package net.oneandone.mork.classfile;

import net.oneandone.sushi.util.IntArrayList;

import java.io.IOException;

/**
 * Encoding of an instruction.
 */

public class InstructionEncoding implements Bytecodes, Constants {
    public final String name;

    public final int[] args;          // argument types for normal opcodes
    public final InstructionType type;

    public InstructionEncoding(String name, int[] args, InstructionType type) {
        this.name = name;
        this.args = args;
        this.type = type;
    }

    //--

    /**
     * all opcodes except for tableswitch, lookupswitch and wide are considered "normal"
     */
    public Instruction read(int opcode, Input src, int ofs) throws IOException {
        switch (opcode) {
        case WIDE:
            return readWide(src, ofs);
        case TABLESWITCH:
            return readTableSwitch(src, ofs);
        case LOOKUPSWITCH:
            return readLookupSwitch(src, ofs);
        default:
            return readNormal(src, ofs);
        }
    }

    private Instruction readTableSwitch(Input src, int ofs) throws IOException {
        Object[] args;
        IntArrayList branches;
        int deflt, low, high;
        int i;
        int max;

        src.readPad();

        deflt = src.readU4();
        low = src.readU4();
        high = src.readU4();
        max = high - low + 1;

        branches = new IntArrayList();

        args = new Object[4];
        args[0] = new Integer(deflt);
        args[1] = new Integer(low);
        args[2] = new Integer(high);
        args[3] = branches;

        for (i = 0; i < max; i++) {
            branches.add(src.readU4());
        }
        return new Instruction(ofs, type, args);
    }

    private Instruction readLookupSwitch(Input src, int ofs) throws IOException {
        int i;
        int count;
        int deflt;
        Object[] args;
        IntArrayList keys;
        IntArrayList branches;

        src.readPad();
        deflt = src.readU4();
        count = src.readU4();
        keys = new IntArrayList();
        branches = new IntArrayList();

        args = new Object[3];
        args[0] = new Integer(deflt);
        args[1] = keys;
        args[2] = branches;

        for (i = 0; i < count; i++) {
            keys.add(src.readU4());
            branches.add(src.readU4());
        }
        return new Instruction(ofs, type, args);
    }

    private static Instruction readWide(Input src, int ofs) throws IOException {
        int b;
        Object[] args;

        b = src.readU1();
        switch (b) {
        case IINC:
            args = new Object[2];
            args[0] = src.readU2();
            args[1] = src.readS2();
            break;
        case ILOAD:
        case FLOAD:
        case ALOAD:
        case LLOAD:
        case DLOAD:
        case ISTORE:
        case FSTORE:
        case ASTORE:
        case LSTORE:
        case DSTORE:
        case RET:
            // TODO: don't hard-wire constants
            args = new Object[] { src.readU2() };
            break;
        default:
            throw new RuntimeException("illegal iinc");
        }
        return new Instruction(ofs, Set.TYPES[b], args);
    }

    private Instruction readNormal(Input src, int ofs) throws IOException {
        Object[] argValues;
        int i;

        argValues = new Object[args.length];
        for (i = 0; i < args.length; i++) {
            argValues[i] = readArg(src, args[i]);
        }

        return new Instruction(ofs, type, argValues);
    }

    private static Object readArg(Input src, int argType) throws IOException {
        MethodRef ifc;
        int tmp;

        switch (argType) {
        case AE_U1:
            return new Integer(src.readU1());
        case AE_S1:
            return new Integer(src.readS1());
        case AE_U2:
            return new Integer(src.readU2());
        case AE_S2:
            return new Integer(src.readS2());
        case AE_U4:
            return new Integer(src.readU4());
        case AE_REFTYPEREF:
            return src.readClassRef();
        case AE_FIELDREF:
            return src.readFieldRef();
        case AE_IFMETHOD:
            ifc = src.readInterfaceMethodRef();
            tmp = src.readU1();
            if (tmp != 1 + ifc.argSize()) { // 1 for "this"
                throw new RuntimeException(
                    ifc + ": illegal ifc arg size: " + tmp);
            }
            if (src.readU1() != 0) {
                throw new RuntimeException("0 expected");
            }
            return ifc;
        case AE_METHODREF:
            return src.readClassMethodRef();
        case AE_CNST:
            // TODO: check type
            return src.readShortConstant();
        case AE_CNST_W:
        case AE_CNST2_W:
            // TODO: check type
            return src.readConstant();
        default:
            if (argType <= AE_I_LAST) {
                return IMPLICIT[argType];
            } else {
                throw new IllegalArgumentException("illegal arg type: " + argType);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result;
        int i;

        result = new StringBuilder();
        result.append(name);
        for (i = 0; i < args.length; i++) {
            result.append(' ');
            result.append("" + args[i]);
        }
        return result.toString();
    }
}
