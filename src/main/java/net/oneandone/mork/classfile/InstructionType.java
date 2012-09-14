/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.mork.classfile;

import net.oneandone.sushi.util.IntArrayList;

import java.io.IOException;

/**
 * Type of an instruction.
 */

public class InstructionType implements Bytecodes, Constants {
    public final int opcode;
    public final String name;
    public final int stackDiff;  // effect on the operand stack
    public final int succType;   // successors

    /**
     * depends on encoding:
     *  SIMPLE:         argument types
     *  LV:             opcode_0, opcode_1, opcode_2, opcode_3
     *  BRANCH:         opcodeNot   // opcode for negation
     *  VBRANCH:        opcodeW
     *  otherwise:      empty
     */
    public final int[] args;
    public final int encoding;
    /** for SIMPLE encodings only; effectively a final variable. */
    public int length;

    public InstructionType(
        String nameInit, int opcodeInit, int[] argsInit,
        int encodingInit, int stackDiffInit, int succTypeInit) {
        name = nameInit;
        opcode = opcodeInit;
        args = argsInit;
        encoding = encodingInit;
        stackDiff = stackDiffInit;
        succType = succTypeInit;
        calcLength();
    }

    public void checkArgs(Object[] argValues) {
        // TODO: add more tests

        switch (encoding) {
        case SIMPLE:
            checkSimpleArgs(argValues);
            break;
        case RT: // RETURN
        case LV:
            if (argValues.length != 1) {
                throw new RuntimeException("1 arg expected: " + this + " " +
                                           argValues);
            }
            if (!(argValues[0] instanceof Integer)) {
                throw new RuntimeException("Integer arg expected: " +
                                           this + " " + argValues[0]);
            }
            break;
        case INC:
            if (argValues.length != 2) {
                throw new RuntimeException("2 args expected: " + this + " " +
                                           argValues);
            }
            if (!(argValues[0] instanceof Integer)) {
                throw new RuntimeException("Integer arg expected: " +
                                           this + " " + argValues[0]);
            }
            if (!(argValues[1] instanceof Integer)) {
                throw new RuntimeException("Integer arg 2 expected: " +
                                           this + " " + argValues[1]);
            }
            break;
        case BRANCH:
        case VBRANCH:
            if (argValues.length != 1) {
                throw new RuntimeException("<>BRANCH: 1 arg expected: " +
                                           argValues.length);
            }
            if (!(argValues[0] instanceof Integer)) {
                throw new RuntimeException("Integer arg expected: " +
                                           this + " " + argValues[0]);
            }
            break;
        case LS:
            if (argValues.length != 3) {
                throw new RuntimeException("LS: 3 arg expected: " +
                                           argValues.length);
            }
            if (!(argValues[0] instanceof Integer)) {
                throw new RuntimeException("Integer arg expected: " +
                                           this + " " + argValues[0]);
            }
            if (!(argValues[1] instanceof IntArrayList)) {
                throw new RuntimeException("IntArrayList arg expected: " +
                                           this + " " + argValues[1]);
            }
            if (!(argValues[2] instanceof IntArrayList)) {
                throw new RuntimeException("IntArrayList arg expected: " +
                                           this + " " + argValues[2]);
            }
            break;
        case TS:
            if (argValues.length != 4) {
                throw new RuntimeException("TS: 4 arg expected: " +
                                           argValues.length);
            }
            if (!(argValues[0] instanceof Integer)) {
                throw new RuntimeException("Integer arg expected: " +
                                           this + " " + argValues[0]);
            }
            if (!(argValues[1] instanceof Integer)) {
                throw new RuntimeException("Integer arg expected: " +
                                           this + " " + argValues[1]);
            }
            if (!(argValues[2] instanceof Integer)) {
                throw new RuntimeException("Integer arg expected: " +
                                           this + " " + argValues[2]);
            }
            if (!(argValues[3] instanceof IntArrayList)) {
                throw new RuntimeException("IntArrayList arg expected: " +
                                           this + " " + argValues[3]);
            }
            break;
        case CNST:
            if (argValues.length != 1) {
                throw new RuntimeException("LDC: 1 arg expected: " +
                                           argValues.length);
            }
            break;
        default:
            throw new RuntimeException("illegal encoding: " + encoding);
        }
    }

    public void checkSimpleArgs(Object[] argValues) {
        int i;
        Object v;

        if (args.length != argValues.length) {
            throw new RuntimeException("illegal argument count: "
                                       + this + ":" + argValues.length);
        }
        for (i = 0; i < args.length; i++) {
            v = argValues[i];
            switch (args[i]) {
            case REFTYPEREF:
                if (!(v instanceof ClassRef)) {
                    throw new RuntimeException("ClassRef expected: " + this
                                               + v);
                }
                break;
            case FIELDREF:
                if (!(v instanceof FieldRef)) {
                    throw new RuntimeException("FieldRef expected: " + this
                                               + v);
                }
                break;
            case IFMETHOD:
                if (!(v instanceof MethodRef)) {
                    throw new RuntimeException("MethodRef expected: " + this
                                               + v);
                }
                break;
            case METHODREF:
                if (!(v instanceof MethodRef)) {
                    throw new RuntimeException("MethodRef expected: " + this
                                               + v);
                }
                break;
            case BYTE:
            case TYPE_BYTE:
                if (!(v instanceof Integer)) {
                    throw new RuntimeException("Integer expected: " + this
                                               + v);
                }
                break;
            default:
                throw new RuntimeException("illegal argument type: " +
                                           args[i]);
            }
        }
    }

    private void calcLength() {
        int i;

        if (encoding != SIMPLE) {
            length = -1;
        } else {
            length = 1;
            for (i = 0; i < args.length; i++) {
                switch (args[i]) {
                case BYTE:
                case TYPE_BYTE:
                    length += 1;
                    break;
                case REFTYPEREF:
                case FIELDREF:
                case METHODREF:
                    length += 2;
                    break;
                case IFMETHOD:
                    // because of "<size> 0" following the index
                    length += 4;
                    break;
                default:
                    throw new IllegalArgumentException(
                       name + ": illegal arg type: " + args[i]);
                }
            }
        }
    }

    @Override
    public String toString() {
        return name + "=" + opcode;
    }

    //--

    /** Fixup. Turn ofs into idx. */
    public void ofsToIdx(Code context, int ofs, Object[] argValues) {
        int i;
        IntArrayList branchesW;

        switch (encoding) {
        case TS:
            i = ((Integer) argValues[0]).intValue();
            argValues[0] = new Integer(context.findIdx(ofs + i));
            branchesW = (IntArrayList) argValues[3];
            for (i = 0; i < branchesW.size(); i++) {
                branchesW.set(i, context.findIdx(ofs + branchesW.get(i)));
            }
            break;
        case LS:
            i = ((Integer) argValues[0]).intValue();
            argValues[0] = new Integer(context.findIdx(ofs + i));
            branchesW = (IntArrayList) argValues[2];
            for (i = 0; i < branchesW.size(); i++) {
                branchesW.set(i, context.findIdx(ofs + branchesW.get(i)));
            }
            break;
        case BRANCH:
        case VBRANCH:
            i = ((Integer) argValues[0]).intValue();
            argValues[0] = new Integer(context.findIdx(ofs + i));
            break;
        default:
            // do nothing
            break;
        }
    }

    //--

    private static int branch(int srcOfs, int destIdx, Output context) {
        return context.getCode().findOfs(destIdx) - srcOfs;
    }

    public void write(Output dest, Object[] values) throws IOException {
        int i, i2;
        IntArrayList ofss;
        IntArrayList keys;
        int ofs;

        switch (encoding) {
        case SIMPLE:
            writeSimple(dest, values);
            break;
        case CNST:
            writeCNST(dest, values[0]);
            break;
        case BRANCH:
            i = branch(dest.getOfs(), ((Integer) values[0]).intValue(), dest);
            if ((Short.MIN_VALUE <= i) && (i <= Short.MAX_VALUE)) {
                dest.writeU1(opcode);
                dest.writeS2(i);
            } else {
                dest.writeU1(args[0]);
                dest.writeS2(8);
                dest.writeU1(Bytecodes.GOTO_W);
                dest.writeU4(i - 3);
            }
            break;
        case VBRANCH:
            i = branch(dest.getOfs(), ((Integer) values[0]).intValue(), dest);
            if ((Short.MIN_VALUE <= i) && (i <= Short.MAX_VALUE)) {
                dest.writeU1(opcode);
                dest.writeS2(i);
            } else {
                dest.writeU1(args[0]);
                dest.writeU4(i);
            }
            break;
        case TS:
            ofs = dest.getOfs();
            dest.writeU1(opcode);
            dest.writePad();
            dest.writeU4(branch(ofs, ((Integer) values[0]).intValue(), dest));
            dest.writeU4(((Integer) values[1]).intValue());
            dest.writeU4(((Integer) values[2]).intValue());
            ofss = (IntArrayList) values[3];
            for (i = 0; i < ofss.size(); i++) {
                dest.writeU4(branch(ofs, ofss.get(i), dest));
            }
            break;
        case LS:
            ofs = dest.getOfs();
            dest.writeU1(opcode);
            dest.writePad();
            dest.writeU4(branch(ofs, ((Integer) values[0]).intValue(), dest));
            keys = (IntArrayList) values[1];
            ofss = (IntArrayList) values[2];
            dest.writeU4(keys.size());
            for (i = 0; i < keys.size(); i++) {
                dest.writeU4(keys.get(i));
                dest.writeU4(branch(ofs, ofss.get(i), dest));
            }
            break;
        case LV:
            i = ((Integer) values[0]).intValue();
            if (i < 0) {
                throw new RuntimeException("LV < 0 " + i);
            } else if (i <= 3) {
                dest.writeU1(args[i]);
            } else if (i <= 255) {
                dest.writeU1(opcode);
                dest.writeU1(i);
            } else if (i <= Character.MAX_VALUE) {
                dest.writeU1(WIDE);
                dest.writeU1(opcode);
                dest.writeU2(i);
            } else {
                throw new RuntimeException();
            }
            break;
        case RT:
            i = ((Integer) values[0]).intValue();
            if (i <= 255) {
                dest.writeU1(opcode);
                dest.writeU1(i);
            } else {
                dest.writeU1(WIDE);
                dest.writeU1(opcode);
                dest.writeU2(i);
            }
            break;
        case INC:
            i = ((Integer) values[0]).intValue();
            i2 = ((Integer) values[1]).intValue();
            if ((i <= 255) &&
                (Byte.MIN_VALUE <= i2) && (i2 <= Byte.MAX_VALUE)) {
                dest.writeU1(opcode);
                dest.writeU1(i);
                dest.writeS1(i2);
            } else {
                dest.writeU1(WIDE);
                dest.writeU1(opcode);
                dest.writeU2(i);
                dest.writeS2(i2);
            }
            break;
        default:
            throw new RuntimeException("unknown encoding: " + encoding);
        }
    }


    private void writeSimple(Output dest, Object[] values) throws IOException {
        int i;
        Object val;
        MethodRef ifc;

        dest.writeU1(opcode);
        for (i = 0; i < args.length; i++) {
            val = values[i];
            switch (args[i]) {
            case REFTYPEREF:
                dest.writeClassRef((ClassRef) val);
                break;
            case FIELDREF:
                dest.writeFieldRef((FieldRef) val);
                break;
            case METHODREF:
                dest.writeClassMethodRef((MethodRef) val);
                break;
            case IFMETHOD:
                ifc = (MethodRef) val;
                dest.writeInterfaceMethodRef((MethodRef) val);
                dest.writeU1(1 + ifc.argSize()); // 1 for "this"
                dest.writeU1(0);
                break;
            case BYTE:
            case TYPE_BYTE:
                dest.writeU1(((Integer) val).intValue());
                break;
            default:
                throw new RuntimeException("illegal argType: " + args[i]);
            }
        }
    }

    public void writeCNST(Output dest, Object val) throws IOException {
        int e, ae;

        e = findConstEncoding(dest, val);
        dest.writeU1(e);
        ae = Set.ENCODING[e].args[0];
        switch (ae) {
        case AE_U1:
            dest.writeU1(((Integer) val).intValue());
            break;
        case AE_S1:
            dest.writeS1(((Integer) val).intValue());
            break;
        case AE_U2:
            dest.writeU2(((Integer) val).intValue());
            break;
        case AE_S2:
            dest.writeS2(((Integer) val).intValue());
            break;
        case AE_CNST:
            if (val instanceof Integer) {
                dest.writeShortInt(((Integer) val).intValue());
            } else if (val instanceof Float) {
                dest.writeShortFloat(((Float) val).floatValue());
            } else {
                dest.writeShortString((String) val);
            }
            break;
        case AE_CNST_W:
            if (val instanceof Integer) {
                dest.writeInt(((Integer) val).intValue());
            } else if (val instanceof Float) {
                dest.writeFloat(((Float) val).floatValue());
            } else {
                dest.writeString((String) val);
            }
            break;
        case AE_CNST2_W:
            if (val instanceof Long) {
                dest.writeLong(((Long) val).longValue());
            } else {
                dest.writeDouble(((Double) val).doubleValue());
            }
            break;
        default:
            if (ae <= AE_I_LAST) {
                // no explicit arguments
            } else {
                throw new IllegalArgumentException(
                    name + ": illegal arg encoding: " + ae);
            }
        }
    }

    //-- length

    public boolean isVariable() {
        switch (encoding) {
        case BRANCH:
        case VBRANCH:
        case TS:
        case LS:
            return true;
        default:
            return false;
        }
    }

    public int getVariableLength(Code context, int ofs, Object[] argValues) {
        int tmp;

        switch (encoding) {
        case TS:
            return 1 + IO.padSize(ofs + 1)
                + 3*4 + ((IntArrayList) argValues[3]).size() * 4;
        case LS:
            return 1 + IO.padSize(ofs + 1)
                + 2*4 + ((IntArrayList) argValues[1]).size() * 4 * 2;
        case BRANCH:
            tmp = context.findOfs(((Integer) argValues[0]).intValue()) - ofs;
            if ((Short.MIN_VALUE <= tmp) && (tmp <= Short.MAX_VALUE)) {
                return 3;
            } else {
                return 8;
            }
        case VBRANCH:
            tmp = context.findOfs(((Integer) argValues[0]).intValue()) - ofs;
            if ((Short.MIN_VALUE <= tmp) && (tmp <= Short.MAX_VALUE)) {
                return 3;
            } else {
                return 5;
            }
        default:
            throw new RuntimeException("not of variable length: " + encoding);
        }
    }

    public int getMaxLength(Output dest, Object[] argValues) {
        int i, i2;

        switch (encoding) {
        case SIMPLE:
            return length;
        case CNST:
            return constLength(dest, argValues[0]);
        case BRANCH:
            return 8;
        case VBRANCH:
            // maximum: 4 byte branch
            return 5;
        case TS:
            // maximum pad size: 3
            return 1 + 3 + 4*3 + ((IntArrayList) argValues[3]).size() * 4;
        case LS:
            // maximum pad size: 3
            return 1 + 3 + 4*2 + ((IntArrayList) argValues[1]).size() * 2 * 4;
        case LV:
            i = ((Integer) argValues[0]).intValue();
            if (i < 0) {
                throw new RuntimeException("LV to small: " + i);
            } else if (i <= 3) {
                return 1;
            } else if (i <= 255) {
                return 2;
            } else if (i <= Character.MAX_VALUE) {
                return 4;
            } else {
                throw new RuntimeException("LV to big: " + i);
            }
        case RT:
            i = ((Integer) argValues[0]).intValue();
            if (i <= 255) {
                return 2;
            } else {
                // wide
                return 4;
            }
        case INC:
            i = ((Integer) argValues[0]).intValue();
            i2 = ((Integer) argValues[1]).intValue();
            if ((i <= 255) &&
                (Byte.MIN_VALUE <= i2) && (i2 <= Byte.MAX_VALUE)) {
                return 3;
            } else {
                return 6;
            }
        default:
            throw new RuntimeException("unknown encoding: " + encoding);
        }
    }

    private int constLength(Output dest, Object cnst) {
        int e;

        e = findConstEncoding(dest, cnst);
        switch (e) {
        case ACONST_NULL:
        case ICONST_M1:
        case ICONST_0:
        case ICONST_1:
        case ICONST_2:
        case ICONST_3:
        case ICONST_4:
        case ICONST_5:
        case LCONST_0:
        case LCONST_1:
        case FCONST_0:
        case FCONST_1:
        case DCONST_0:
        case DCONST_1:
            return 1;
        case BIPUSH:
        case LDC:
            return 2;
        case SIPUSH:
        case LDC_W:
        case LDC2_W:
            return 3;
        default:
            throw new RuntimeException("illegal constant: " + e);
        }
    }

    private int findConstEncoding(Output dest, Object cnst) {
        boolean twice;
        int idx;
        int v;

        if (cnst == null) {
            return ACONST_NULL;
        } else if (cnst instanceof Integer) {
            v = ((Integer) cnst).intValue();
            switch (v) {
            case -1:
                return ICONST_M1;
            case 0:
                return ICONST_0;
            case 1:
                return ICONST_1;
            case 2:
                return ICONST_2;
            case 3:
                return ICONST_3;
            case 4:
                return ICONST_4;
            case 5:
                return ICONST_5;
            default:
                if ((Byte.MIN_VALUE <= v) && (v <= Byte.MAX_VALUE)) {
                    return BIPUSH;
                } else if ((Short.MIN_VALUE <= v) && (v <= Short.MAX_VALUE)) {
                    return SIPUSH;
                } else {
                    twice = false;
                    idx = dest.addIfNew(CONSTANT_INTEGER, cnst);
                    // don't return
                }
            }
        } else if (cnst instanceof String) {
            twice = false;
            idx = dest.addIfNew(CONSTANT_STRING, cnst);
            // don't return
        } else if (cnst instanceof Long) {
            if (IMPLICIT[AE_I_L0].equals(cnst)) {
                return LCONST_0;
            } else if (IMPLICIT[AE_I_L1].equals(cnst)) {
                return LCONST_1;
            } else {
                twice = true;
                idx = dest.addIfNew(CONSTANT_LONG, cnst);
                // don't return
            }
        } else if (cnst instanceof Float) {
            if (IMPLICIT[AE_I_F0].equals(cnst)) {
                return FCONST_0;
            } else if (IMPLICIT[AE_I_F1].equals(cnst)) {
                return FCONST_1;
            } else {
                twice = false;
                idx = dest.addIfNew(CONSTANT_FLOAT, cnst);
            }
        } else if (cnst instanceof Double) {
            if (IMPLICIT[AE_I_D0].equals(cnst)) {
                return DCONST_0;
            } else if (IMPLICIT[AE_I_D1].equals(cnst)) {
                return DCONST_1;
            } else {
                twice = true;
                idx = dest.addIfNew(CONSTANT_DOUBLE, cnst);
                // don't return;
            }
        } else {
            throw new RuntimeException("illegal constant: " + cnst);
        }

        if (!twice && (idx <= 255)) {
            return LDC;
        } else {
            return (twice)? LDC2_W : LDC_W;
        }
    }
}
