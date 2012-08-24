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
package net.sf.beezle.mork.classfile;

import net.sf.beezle.sushi.util.IntArrayList;
import net.sf.beezle.sushi.util.IntCollection;

import java.io.IOException;
import java.util.List;

/**
 * Stupid data container.
 */
public class Instruction implements Constants {
    /**
     * Only valid when reading and writing instructions.
     */
    public int ofs;
    public final InstructionType type;
    public final Object[] arguments;

    public Instruction(int ofsInit, InstructionType typeInit, Object[] argumentsInit) {
        ofs = ofsInit;
        type = typeInit;
        arguments = argumentsInit;

        // TODO: this is expensive, but good for initial testing
        //  move into Code.emit some day
        type.checkArgs(arguments);
    }

    public int getMaxLength(Output dest) {
        return type.getMaxLength(dest, arguments);
    }

    public static Instruction read(Input src) throws IOException {
        int opcode;
        int ofs;
        InstructionEncoding encoding;

        ofs = src.getOfs();
        opcode = src.readU1();
        encoding = Set.ENCODING[opcode];
        if (encoding == null) {
            throw new RuntimeException("illegal opcode: " + opcode);
        } else {
            return encoding.read(opcode, src, ofs);
        }
    }

    public void write(Output dest) throws IOException {
        type.write(dest, arguments);
    }

    public void ofsToIdx(Code context) {
        type.ofsToIdx(context, ofs, arguments);
    }

    public int getVariableLength(Code context) {
        return type.getVariableLength(context, ofs, arguments);
    }

    /**
     * @param code used to resolve labels
     */
    public void getSuccessors(List<Jsr> jsrs, int idx, Code code, IntCollection result) {
        int i;
        IntArrayList tmp;
        int max;

        switch (type.succType) {
        case SUCC_NONE:
            break; // no successor
        case SUCC_NEXT:
            result.add(idx + 1);
            break;
        case SUCC_GOTO:
            result.add(code.resolveLabel((Integer) arguments[0]));
            break;
        case SUCC_BRANCH:
            result.add(idx + 1);
            result.add(code.resolveLabel((Integer) arguments[0]));
            break;
        case SUCC_LOOKUPSWITCH:
            result.add(code.resolveLabel((Integer) arguments[0]));
            tmp = (IntArrayList) arguments[2];
            max = tmp.size();
            for (i = 0; i < max; i++) {
                result.add(code.resolveLabel(tmp.get(i)));
            }
            break;
        case SUCC_TABLESWITCH:
            result.add(code.resolveLabel((Integer) arguments[0]));
            tmp = (IntArrayList) arguments[3];
            max = tmp.size();
            for (i = 0; i < max; i++) {
                result.add(code.resolveLabel(tmp.get(i)));
            }
            break;
        case SUCC_JSR:
            result.add(code.resolveLabel((Integer) arguments[0]));
            break;
        case SUCC_RET:
            Jsr.addRetSuccessors(jsrs, idx, result);
            break;
        default:
            throw new RuntimeException("successor not supported: " + type.succType);
        }
    }

    public int getStackDiff() {
        MethodRef m;
        Object obj;

        switch (type.stackDiff) {
        case MULTIARRAY_STACK:
            return -((Integer) arguments[1]).intValue() + 1;
        case INVOKEVIRTUAL_STACK:
            m = (MethodRef) arguments[0];
            return -1 - m.argSize() + m.returnType.operandSize();
        case INVOKESPECIAL_STACK:
            m = (MethodRef) arguments[0];
            return -1 - m.argSize() + m.returnType.operandSize();
        case INVOKESTATIC_STACK:
            m = (MethodRef) arguments[0];
            return - m.argSize() + m.returnType.operandSize();
        case INVOKEINTERFACE_STACK:
            m = (MethodRef) arguments[0];
            return -1 - m.argSize() + m.returnType.operandSize();
        case GETSTATIC_STACK:
            return ((FieldRef) arguments[0]).type.operandSize();
        case PUTSTATIC_STACK:
            return -((FieldRef) arguments[0]).type.operandSize();
        case GETFIELD_STACK:
            return -1 + ((FieldRef) arguments[0]).type.operandSize();
        case PUTFIELD_STACK:
            return -1 - ((FieldRef) arguments[0]).type.operandSize();
        case LDC_STACK:
            obj = arguments[0];
            if (obj == null) {
                return 1;
            } else if (obj instanceof Long) {
                return 2;
            } else if (obj instanceof Double) {
                return 2;
            } else if (obj instanceof Object) {
                return 1;
            }
        default:
            if (type.stackDiff >= ERROR_STACK) {
                throw new RuntimeException("illegal stackDiff: "
                                           + type.stackDiff);
            }
            return type.stackDiff;
        }
    }

    @Override
    public String toString() {
        StringBuilder result;
        int i;

        result = new StringBuilder();
        result.append(type.name);
        for (i = 0; i < arguments.length; i++) {
            result.append(' ');
            result.append("" + arguments[i]);  // arg might be null
        }
        return result.toString();
    }
}
