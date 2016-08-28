/*
 * Copyright (c) 2010, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * Represents a predicate (boolean-valued function) of one {@code double}-valued
 * argument. This is the {@code double}-consuming primitive type specialization
 * of {@link Predicate}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(double)}.
 *
 * @see Predicate
 * @since 1.8
 */
@FunctionalInterface
public interface DoublePredicate {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param value the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    boolean test(double value);

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * AND of this predicate and another.  When evaluating the composed
     * predicate, if this predicate is {@code false}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ANDed with this
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * AND of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    DoublePredicate and(DoublePredicate other);

    /**
     * Returns a predicate that represents the logical negation of this
     * predicate.
     *
     * @return a predicate that represents the logical negation of this
     * predicate
     */
    DoublePredicate negate();

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * OR of this predicate and another.  When evaluating the composed
     * predicate, if this predicate is {@code true}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ORed with this
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * OR of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    DoublePredicate or(DoublePredicate other);

    abstract class $ implements DoublePredicate {

        @Override
        public DoublePredicate and(DoublePredicate other) {
            return $.and(this, other);
        }

        @Override
        public DoublePredicate negate() {
            return $.negate(this);
        }

        @Override
        public DoublePredicate or(DoublePredicate other) {
            return $.or(this, other);
        }

        public static DoublePredicate and(final DoublePredicate $this, final DoublePredicate other) {
            Objects.requireNonNull(other);
            return new $() {
                @Override
                public boolean test(double value) {
                    return $this.test(value) && other.test(value);
                }
            };
        }

        public static DoublePredicate negate(final DoublePredicate $this) {
            return new $() {
                @Override
                public boolean test(double value) {
                    return !$this.test(value);
                }
            };
        }

        public static DoublePredicate or(final DoublePredicate $this, final DoublePredicate other) {
            Objects.requireNonNull(other);
            return new $() {
                @Override
                public boolean test(double value) {
                    return $this.test(value) || other.test(value);
                }
            };
        }
    }
}
