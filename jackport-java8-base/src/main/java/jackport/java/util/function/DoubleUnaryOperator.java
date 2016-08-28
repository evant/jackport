/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package jackport.java.util.function;

import jackport.java.lang.FunctionalInterface;
import jackport.java.util.Objects;

/**
 * Represents an operation on a single {@code double}-valued operand that produces
 * a {@code double}-valued result.  This is the primitive type specialization of
 * {@link UnaryOperator} for {@code double}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #applyAsDouble(double)}.
 *
 * @see UnaryOperator
 * @since 1.8
 */
@FunctionalInterface
public interface DoubleUnaryOperator {

    /**
     * Applies this operator to the given operand.
     *
     * @param operand the operand
     * @return the operator result
     */
    double applyAsDouble(double operand);

    /**
     * Returns a composed operator that first applies the {@code before}
     * operator to its input, and then applies this operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to
     * the caller of the composed operator.
     *
     * @param before the operator to apply before this operator is applied
     * @return a composed operator that first applies the {@code before}
     * operator and then applies this operator
     * @throws NullPointerException if before is null
     * @see #andThen(DoubleUnaryOperator)
     */
    DoubleUnaryOperator compose(DoubleUnaryOperator before);

    /**
     * Returns a composed operator that first applies this operator to
     * its input, and then applies the {@code after} operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to
     * the caller of the composed operator.
     *
     * @param after the operator to apply after this operator is applied
     * @return a composed operator that first applies this operator and then
     * applies the {@code after} operator
     * @throws NullPointerException if after is null
     * @see #compose(DoubleUnaryOperator)
     */
    DoubleUnaryOperator andThen(DoubleUnaryOperator after);

    abstract class $ implements DoubleUnaryOperator {

        private static final DoubleUnaryOperator IDENTITY = new $() {
            @Override
            public double applyAsDouble(double operand) {
                return operand;
            }
        };

        @Override
        public DoubleUnaryOperator compose(DoubleUnaryOperator before) {
            return $.compose(this, before);
        }

        @Override
        public DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
            return $.andThen(this, after);
        }

        public static DoubleUnaryOperator compose(final DoubleUnaryOperator $this, final DoubleUnaryOperator before) {
            Objects.requireNonNull(before);
            return new $() {
                @Override
                public double applyAsDouble(double v) {
                    return $this.applyAsDouble(before.applyAsDouble(v));
                }
            };
        }

        public static DoubleUnaryOperator andThen(final DoubleUnaryOperator $this, final DoubleUnaryOperator after) {
            Objects.requireNonNull(after);
            return new $() {
                @Override
                public double applyAsDouble(double t) {
                    return after.applyAsDouble($this.applyAsDouble(t));
                }
            };
        }

        /**
         * Returns a unary operator that always returns its input argument.
         *
         * @return a unary operator that always returns its input argument
         */
        public static DoubleUnaryOperator identity() {
            return IDENTITY;
        }
    }
}
