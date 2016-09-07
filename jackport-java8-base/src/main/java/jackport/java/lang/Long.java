/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jackport.java.lang;

/**
 * The {@code Integer} class wraps a value of the primitive type
 * {@code int} in an object. An object of type {@code Integer}
 * contains a single field whose type is {@code int}.
 *
 * <p>In addition, this class provides several methods for converting
 * an {@code int} to a {@code String} and a {@code String} to an
 * {@code int}, as well as other constants and methods useful when
 * dealing with an {@code int}.
 *
 * <p>Implementation note: The implementations of the "bit twiddling"
 * methods (such as {@link java.lang.Integer#highestOneBit(int) highestOneBit} and
 * {@link java.lang.Integer#numberOfTrailingZeros(int) numberOfTrailingZeros}) are
 * based on material from Henry S. Warren, Jr.'s <i>Hacker's
 * Delight</i>, (Addison Wesley, 2002).
 *
 * @author Lee Boynton
 * @author Arthur van Hoff
 * @author Josh Bloch
 * @author Joseph D. Darcy
 * @since JDK1.0
 */
public final class Long {

    /**
     * The number of bytes used to represent a {@code int} value in two's
     * complement binary form.
     *
     * @since 1.8
     */
    public static final int BYTES = java.lang.Long.SIZE / Byte.SIZE;

    /**
     * Returns a hash code for a {@code long} value; compatible with
     * {@code Long.hashCode()}.
     *
     * @param value the value to hash
     * @return a hash code value for a {@code long} value.
     * @since 1.8
     */
    public static int hashCode(long value) {
        return (int) (value ^ (value >>> 32));
    }

    /**
     * Adds two {@code long} values together as per the + operator.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the sum of {@code a} and {@code b}
     * @see jackport.java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long sum(long a, long b) {
        return a + b;
    }

    /**
     * Returns the greater of two {@code long} values
     * as if by calling {@link Math#max(long, long) Math.max}.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the greater of {@code a} and {@code b}
     * @see jackport.java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long max(long a, long b) {
        return Math.max(a, b);
    }

    /**
     * Returns the smaller of two {@code long} values
     * as if by calling {@link Math#min(long, long) Math.min}.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the smaller of {@code a} and {@code b}
     * @see jackport.java.util.function.BinaryOperator
     * @since 1.8
     */
    public static long min(long a, long b) {
        return Math.min(a, b);
    }
}