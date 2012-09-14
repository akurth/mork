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

/**
 * Constants for this implementations.  TODO: remove public
 */

public interface Constants {
    //-- constants that originate from JVM spec

    int MAGIC = 0xcafebabe;

    // primitive type coding for newarray
    byte T_REFERENCE = 2; // not an official value
    byte T_VOID = 3;      // not an official value
    byte T_BOOLEAN = 4;
    byte T_CHAR = 5;
    byte T_FLOAT = 6;
    byte T_DOUBLE = 7;
    byte T_BYTE = 8;
    byte T_SHORT = 9;
    byte T_INT = 10;
    byte T_LONG = 11;

    byte CONSTANT_CLASS              = 7;
    byte CONSTANT_FIELDREF           = 9;
    byte CONSTANT_METHODREF          = 10;
    byte CONSTANT_INTERFACEMETHODREF = 11;
    byte CONSTANT_STRING             = 8;
    byte CONSTANT_INTEGER            = 3;
    byte CONSTANT_FLOAT              = 4;
    byte CONSTANT_LONG               = 5;
    byte CONSTANT_DOUBLE             = 6;
    byte CONSTANT_NAMEANDTYPE        = 12;
    byte CONSTANT_UTF8               = 1;

    //-- InstructionType encoding.
    int SIMPLE  = 0;
    int LV      = 1;
    int INC     = 2;
    int RT      = 3;
    int BRANCH  = 4;
    int VBRANCH = 5;
    int LS      = 6;
    int TS      = 7;
    int CNST    = 8;  // constants

    // Argument types coding for simple instructions
    int REFTYPEREF = 0;
    int FIELDREF   = 1;
    int IFMETHOD   = 2;
    int METHODREF  = 3;
    int BYTE       = 4;
    int TYPE_BYTE  = 5;

    // succ type
    int SUCC_NONE = 0;
    int SUCC_NEXT = 1;
    int SUCC_GOTO = 2;
    int SUCC_BRANCH= 3;
    int SUCC_LOOKUPSWITCH = 4;
    int SUCC_TABLESWITCH = 5;
    int SUCC_JSR = 6;
    int SUCC_RET = 7;

    //-- special values for stackdiff

    int MULTIARRAY_STACK      = 1011;
    int INVOKEVIRTUAL_STACK   = 1012;
    int INVOKESPECIAL_STACK   = 1013;
    int INVOKESTATIC_STACK    = 1014;
    int INVOKEINTERFACE_STACK = 1015;
    int GETSTATIC_STACK       = 1016;
    int PUTSTATIC_STACK       = 1017;
    int GETFIELD_STACK        = 1018;
    int PUTFIELD_STACK        = 1019;
    int LDC_STACK             = 1020;
    int ERROR_STACK           = 1021;
       // start of illegal values

    //-- argument encoding (AE)

    // implicit argumtents; they come first. _I_ stands for implicit
    int AE_I_NULL =  0;
    int AE_I_IML  =  1;
    int AE_I_I0   =  2;
    int AE_I_I1   =  3;
    int AE_I_I2   =  4;
    int AE_I_I3   =  5;
    int AE_I_I4   =  6;
    int AE_I_I5   =  7;
    int AE_I_L0   =  8;
    int AE_I_L1   =  9;
    int AE_I_F0   = 10;
    int AE_I_F1   = 11;
    int AE_I_F2   = 12;
    int AE_I_D0   = 13;
    int AE_I_D1   = 14;

    int AE_I_LAST = AE_I_D1;

    /** 1 byte of unsigned immediate data. */
    int AE_U1 = 15;

    /** 1 byte of signed immediate data. */
    int AE_S1 = 16;

    /** 2 byte of unsigned immediate data. */
    int AE_U2 = 17;

    /** 2 byte of signed immediate data. */
    int AE_S2 = 18;

    /** 4 byte data. */
    int AE_U4 = 19;

    /** 2 byte index to reference of array, class or interface. */
    int AE_REFTYPEREF = 20;

    int AE_FIELDREF = 21;

    int AE_IFMETHOD = 22;

    int AE_METHODREF = 23;

    /** 1 byte index to int or float or String constant. */
    int AE_CNST = 24;

    /** 2 byte index to int or float or string constant. */
    int AE_CNST_W = 25;

    /** 2 byte index to long or double constant. */
    int AE_CNST2_W = 26;

    // values for implicit physical arguments
    Object[] IMPLICIT = {
        null,
        -1,
        0,
        1,
        2,
        3,
        4,
        5,
        new Long(0),
        new Long(1),
        new Float(0),
        new Float(1),
        new Float(2),
        new Double(0),
        new Double(1),
    };
}
