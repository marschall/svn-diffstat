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

import static org.threeten.bp.temporal.ChronoUnit.YEARS;

import java.io.Serializable;

import org.threeten.bp.temporal.TemporalUnit;

/**
 * An amount of time measured in years, such as '6 Years'.
 * <p>
 * This class stores an amount of time in terms of the years unit of time.
 *
 * <h3>Specification for implementors</h3>
 * This class is immutable and thread-safe.
 */
public final class Years extends AbstractSimpleAmount<Years> implements Serializable {

    /**
     * A constant for zero years.
     */
    public static final Years ZERO = new Years(0);
    /**
     * A serialization identifier for this class.
     */
    private static final long serialVersionUID = -8903767091325669093L;

    /**
     * The number of years.
     */
    private final int years;

    /**
     * Obtains an instance of {@code Years}.
     *
     * @param years  the number of years the instance will represent, may be negative
     * @return the {@code Years} instance, not null
     */
    public static Years of(int years) {
        if (years == 0) {
            return ZERO;
        }
        return new Years(years);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructs an instance using a specific number of years.
     *
     * @param years  the years to use
     */
    private Years(int years) {
        super();
        this.years = years;
    }

    /**
     * Resolves singletons.
     *
     * @return the singleton instance
     */
    private Object readResolve() {
        return Years.of(years);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number of years in this amount.
     *
     * @return the number of years, may be negative
     */
    @Override
    public int getAmount() {
        return years;
    }

    /**
     * Returns a new instance of the subclass with a different number of years.
     *
     * @param amount  the number of years to set in the new instance, may be negative
     * @return a new period element, not null
     */
    @Override
    public Years withAmount(int amount) {
        return Years.of(amount);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the unit defining the amount of time.
     *
     * @return the years unit, not null
     */
    @Override
    public TemporalUnit getUnit() {
        return YEARS;
    }

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
    public Years plus(Years amount) {
        return plus(amount.getAmount());
    }

    /**
     * Returns a new instance with the specified amount of time subtracted.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param amount  the amount of time to add, may be negative
     * @return the new amount minus the specified amount of time, not null
     * @throws ArithmeticException if the result overflows an {@code int}
     */
    public Years minus(Years amount) {
        return minus(amount.getAmount());
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string representation of the number of years.
     * This will be in the format 'PnY' where n is the number of years.
     *
     * @return the number of years in ISO8601 string format
     */
    @Override
    public String toString() {
        return "P" + years + "Y";
    }

}
