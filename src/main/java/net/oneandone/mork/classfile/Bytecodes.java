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
 * Some of the constants declared in
 * the Java Virtual Machine Specification.
 */

public interface Bytecodes {
    int AALOAD          = 0x32;
    int AASTORE         = 0x53;
    int ACONST_NULL     = 0x01;
    int ALOAD           = 0x19;
    int ALOAD_0         = 0x2a;
    int ALOAD_1         = 0x2b;
    int ALOAD_2         = 0x2c;
    int ALOAD_3         = 0x2d;
    int ANEWARRAY       = 0xbd;
    int ARETURN         = 0xb0;
    int ARRAYLENGTH     = 0xbe;
    int ASTORE          = 0x3a;
    int ASTORE_0        = 0x4b;
    int ASTORE_1        = 0x4c;
    int ASTORE_2        = 0x4d;
    int ASTORE_3        = 0x4e;
    int ATHROW          = 0xbf;
    int BALOAD          = 0x33;
    int BASTORE         = 0x54;
    int BIPUSH          = 0x10;
    int CALOAD          = 0x34;
    int CASTORE         = 0x55;
    int CHECKCAST       = 0xc0;
    int D2F             = 0x90;
    int D2I             = 0x8e;
    int D2L             = 0x8f;
    int DADD            = 0x63;
    int DALOAD          = 0x31;
    int DASTORE         = 0x52;
    int DCMPG           = 0x98;
    int DCMPL           = 0x97;
    int DCONST_0        = 0x0e;
    int DCONST_1        = 0x0f;
    int DDIV            = 0x6f;
    int DLOAD           = 0x18;
    int DLOAD_0         = 0x26;
    int DLOAD_1         = 0x27;
    int DLOAD_2         = 0x28;
    int DLOAD_3         = 0x29;
    int DMUL            = 0x6b;
    int DNEG            = 0x77;
    int DREM            = 0x73;
    int DRETURN         = 0xaf;
    int DSTORE          = 0x39;
    int DSTORE_0        = 0x47;
    int DSTORE_1        = 0x48;
    int DSTORE_2        = 0x49;
    int DSTORE_3        = 0x4a;
    int DSUB            = 0x67;
    int DUP             = 0x59;
    int DUP_X1          = 0x5a;
    int DUP_X2          = 0x5b;
    int DUP2            = 0x5c;
    int DUP2_X1         = 0x5d;
    int DUP2_X2         = 0x5e;
    int F2D             = 0x8d;
    int F2I             = 0x8b;
    int F2L             = 0x8c;
    int FADD            = 0x62;
    int FALOAD          = 0x30;
    int FASTORE         = 0x51;
    int FCMPG           = 0x96;
    int FCMPL           = 0x95;
    int FCONST_0        = 0x0b;
    int FCONST_1        = 0x0c;
    int FCONST_2        = 0x0d;
    int FDIV            = 0x6e;
    int FLOAD           = 0x17;
    int FLOAD_0         = 0x22;
    int FLOAD_1         = 0x23;
    int FLOAD_2         = 0x24;
    int FLOAD_3         = 0x25;
    int FMUL            = 0x6a;
    int FNEG            = 0x76;
    int FREM            = 0x72;
    int FRETURN         = 0xae;
    int FSTORE          = 0x38;
    int FSTORE_0        = 0x43;
    int FSTORE_1        = 0x44;
    int FSTORE_2        = 0x45;
    int FSTORE_3        = 0x46;
    int FSUB            = 0x66;
    int GETFIELD        = 0xb4;
    int GETSTATIC       = 0xb2;
    int GOTO            = 0xa7;
    int GOTO_W          = 0xc8;
    int I2B             = 0x91;
    int I2C             = 0x92;
    int I2D             = 0x87;
    int I2F             = 0x86;
    int I2L             = 0x85;
    int I2S             = 0x93;
    int IADD            = 0x60;
    int IALOAD          = 0x2e;
    int IAND            = 0x7e;
    int IASTORE         = 0x4f;
    int ICONST_M1       = 0x02;
    int ICONST_0        = 0x03;
    int ICONST_1        = 0x04;
    int ICONST_2        = 0x05;
    int ICONST_3        = 0x06;
    int ICONST_4        = 0x07;
    int ICONST_5        = 0x08;
    int IDIV            = 0x6c;
    int IF_ACMPEQ       = 0xa5;
    int IF_ACMPNE       = 0xa6;
    int IF_ICMPEQ       = 0x9f;
    int IF_ICMPNE       = 0xa0;
    int IF_ICMPLT       = 0xa1;
    int IF_ICMPGE       = 0xa2;
    int IF_ICMPGT       = 0xa3;
    int IF_ICMPLE       = 0xa4;
    int IFEQ            = 0x99;
    int IFNE            = 0x9a;
    int IFLT            = 0x9b;
    int IFGE            = 0x9c;
    int IFGT            = 0x9d;
    int IFLE            = 0x9e;
    int IFNONNULL       = 0xc7;
    int IFNULL          = 0xc6;
    int IINC            = 0x84;
    int ILOAD           = 0x15;
    int ILOAD_0         = 0x1a;
    int ILOAD_1         = 0x1b;
    int ILOAD_2         = 0x1c;
    int ILOAD_3         = 0x1d;
    int IMUL            = 0x68;
    int INEG            = 0x74;
    int INSTANCEOF      = 0xc1;
    int INVOKEINTERFACE = 0xb9;
    int INVOKESPECIAL   = 0xb7;
    int INVOKESTATIC    = 0xb8;
    int INVOKEVIRTUAL   = 0xb6;
    int IOR             = 0x80;
    int IREM            = 0x70;
    int IRETURN         = 0xac;
    int ISHL            = 0x78;
    int ISHR            = 0x7a;
    int ISTORE          = 0x36;
    int ISTORE_0        = 0x3b;
    int ISTORE_1        = 0x3c;
    int ISTORE_2        = 0x3d;
    int ISTORE_3        = 0x3e;
    int ISUB            = 0x64;
    int IUSHR           = 0x7c;
    int IXOR            = 0x82;
    int JSR             = 0xa8;
    int JSR_W           = 0xc9;
    int L2D             = 0x8a;
    int L2F             = 0x89;
    int L2I             = 0x88;
    int LADD            = 0x61;
    int LALOAD          = 0x2f;
    int LAND            = 0x7f;
    int LASTORE         = 0x50;
    int LCMP            = 0x94;
    int LCONST_0        = 0x09;
    int LCONST_1        = 0x0a;
    int LDC             = 0x12;
    int LDC_W           = 0x13;
    int LDC2_W          = 0x14;
    int LDIV            = 0x6d;
    int LLOAD           = 0x16;
    int LLOAD_0         = 0x1e;
    int LLOAD_1         = 0x1f;
    int LLOAD_2         = 0x20;
    int LLOAD_3         = 0x21;
    int LMUL            = 0x69;
    int LNEG            = 0x75;
    int LOOKUPSWITCH    = 0xab;
    int LOR             = 0x81;
    int LREM            = 0x71;
    int LRETURN         = 0xad;
    int LSHL            = 0x79;
    int LSHR            = 0x7b;
    int LSTORE          = 0x37;
    int LSTORE_0        = 0x3f;
    int LSTORE_1        = 0x40;
    int LSTORE_2        = 0x41;
    int LSTORE_3        = 0x42;
    int LSUB            = 0x65;
    int LUSHR           = 0x7d;
    int LXOR            = 0x83;
    int MONITORENTER    = 0xc2;
    int MONITOREXIT     = 0xc3;
    int MULTIANEWARRAY  = 0xc5;
    int NEW             = 0xbb;
    int NEWARRAY        = 0xbc;
    int NOP             = 0x00;
    int POP             = 0x57;
    int POP2            = 0x58;
    int PUTFIELD        = 0xb5;
    int PUTSTATIC       = 0xb3;
    int RET             = 0xa9;
    int RETURN          = 0xb1;
    int SALOAD          = 0x35;
    int SASTORE         = 0x56;
    int SIPUSH          = 0x11;
    int SWAP            = 0x5f;
    int TABLESWITCH     = 0xaa;
    int WIDE            = 0xc4;
}
