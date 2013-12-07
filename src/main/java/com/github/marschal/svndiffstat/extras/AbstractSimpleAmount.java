/*
 * Copyright (c) 2007-2013, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.marschal.svndiffstat.extras;

import org.threeten.bp.jdk8.Jdk8Methods;
import org.threeten.bp.temporal.TemporalUnit;

/**
 * An abstract amount of time measured in terms of a single field,
 * such as days or seconds.
 * <p>
 * This class exists to share code between the public implementations.
 *
 * <h3>Specification for implementors</h3>
 * This is an abstract class and must be implemented with care to ensure
 * other classes in the framework operate correctly.
 * All instantiable subclasses must be final, immutable and thread-safe.
 * 
 * @param <T>  the subclass type
 */
abstract class AbstractSimpleAmount<T extends AbstractSimpleAmount<T>> implements Comparable<T> {
    // amount stored in subclass for serialization reasons

    /**
     * Constructs a new instance.
     */
    AbstractSimpleAmount() {
        super();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the amount of time.
     *
     * @return the amount of time, may be negative
     */
    public abstract int getAmount();

    /**
     * Returns a new instance of the subclass with a different amount of time.
     *
     * @param amount  the new amount of time, may be negative
     * @return a new amount, not null
     */
    public abstract T withAmount(int amount);

    //-----------------------------------------------------------------------
    /**
     * Gets the unit defining the amount of time.
     *
     * @return the unit, not null
     */
    public abstract TemporalUnit getUnit();

    //-----------------------------------------------------------------------
    /**
     * Returns a new instance with the specified amount of time added.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amount  the amount of time to add, may be negative
     * @return the new amount plus the specified amount of time, not null
     * @throws ArithmeticException if the result overflows an {@code int}
     */
    public T plus(int amount) {
        if (amount == 0) {
            @SuppressWarnings("unchecked")
            T result = (T) this;
            return result;
        }
        return withAmount(Jdk8Methods.safeAdd(getAmount(), amount));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a new instance with the specified amount of time subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amount  the amount of time to take away, may be negative
     * @return the new amount minus the specified amount of time, not null
     * @throws ArithmeticException if the result overflows an {@code int}
     */
    public T minus(int amount) {
        return (amount == Integer.MIN_VALUE ? plus(Integer.MAX_VALUE).plus(1) : plus(-amount));
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a new instance with the amount multiplied by the specified scalar.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param scalar  the amount to multiply by, may be negative
     * @return the new amount multiplied by the specified scalar, not null
     * @throws ArithmeticException if the result overflows an {@code int}
     */
    public T multipliedBy(int scalar) {
        return withAmount(Jdk8Methods.safeMultiply(getAmount(), scalar));
    }

    /**
     * Returns a new instance with the amount divided by the specified divisor.
     * The calculation uses integer division, thus 3 divided by 2 is 1.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param divisor  the amount to divide by, may be negative
     * @return the new amount divided by the specified divisor, not null
     * @throws ArithmeticException if the divisor is zero
     */
    public T dividedBy(int divisor) {
        if (divisor == 1) {
            @SuppressWarnings("unchecked")
            T result = (T) this;
            return result;
        }
        return withAmount(getAmount() / divisor);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a new instance with the amount negated.
     *
     * @return the new amount with a negated amount, not null
     * @throws ArithmeticException if the result overflows an {@code int}
     */
    public T negated() {
        return withAmount(safeNegate(getAmount()));
    }

    /**
     * Negates the input value, throwing an exception if an overflow occurs.
     *
     * @param value  the value to negate
     * @return the negated value
     * @throws ArithmeticException if the value is MIN_VALUE and cannot be negated
     */
    private static int safeNegate(int value) {
        if (value == Integer.MIN_VALUE) {
            throw new ArithmeticException("Integer.MIN_VALUE cannot be negated");
        }
        return -value;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares the amount of time in this instance to another instance.
     *
     * @param other  the other amount, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if the other amount is null
     */
    @Override
    public int compareTo(T other) {
        int thisValue = this.getAmount();
        int otherValue = other.getAmount();
        return (thisValue < otherValue ? -1 : (thisValue == otherValue ? 0 : 1));
    }

    /**
     * Checks if the amount of time in this instance greater than that in another instance.
     *
     * @param other  the other amount, not null
     * @return true if this amount is greater
     * @throws NullPointerException if the other amount is null
     */
    public boolean isGreaterThan(T other) {
        return compareTo(other) > 0;
    }

    /**
     * Checks if the amount of time in this instance less than that in another instance.
     *
     * @param other  the other amount, not null
     * @return true if this amount is less
     * @throws NullPointerException if the other amount is null
     */
    public boolean isLessThan(T other) {
        return compareTo(other) < 0;
    }

    //-----------------------------------------------------------------------
    /**
     * Is this instance equal to that specified.
     *
     * @param obj  the other amount of time, null returns false
     * @return true if this amount of time is the same as that specified
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
           return true;
        }
        if (obj instanceof AbstractSimpleAmount<?>) {
            AbstractSimpleAmount<?> other = (AbstractSimpleAmount<?>) obj;
            return getAmount() == other.getAmount() && getUnit().equals(other.getUnit());
        }
        return false;
    }

    /**
     * Returns the hash code for this amount.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return getUnit().hashCode() ^ getAmount();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string representation of the amount of time.
     *
     * @return the amount of time in ISO8601 string format
     */
    @Override
    public abstract String toString();

}
