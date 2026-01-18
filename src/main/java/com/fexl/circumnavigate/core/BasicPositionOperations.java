/*
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.fexl.circumnavigate.core;

/**
 * Basic <b>Position</b> transformations and checks.
 * <br>{@code wrap} and {@code unwrap} transform <b>Positions</b> between coordinate spaces using <b>Position</b> wrapping and unwrapping.
 * <br>{@code isOver} checks if the <b>Position</b> exceeds a <b>Bounds</b>.
 * @param <T> The type of <b>Position</b> the transformations and checks will be used for.
 */
public class BasicPositionOperations<T> {
	/**
	 * Wraps a <b>Position</b> around a <b>Bounds</b> until it is limited/constrained to the <b>Bounds</b> or falls/fits within the <b>Bounds</b>.
	 * @param pos The <b>Position</b> to be <b>Wrapped</b> to the <b>Bounds</b>.
	 * @return {@code wrappedPos} The <b>Wrapped Position</b>.
	 */
	public T wrap(T pos) {
		throw new UnsupportedOperationException("The " + pos.getClass().getSimpleName() + " type has no wrapping method!");
	}

	/**
	 * Returns the closest equivalent <b>Position</b> of {@code wrappedPos} to {@code refPos}
	 * @param refPos The reference <b>Position</b> to be relative to.
	 * @param wrappedPos A <b>Wrapped Position</b>.
	 * @return An unwrapped position
	 */
	public T unwrap(T refPos, T wrappedPos) {
		throw new UnsupportedOperationException("The \"" + wrappedPos.getClass().getSimpleName() + "\" type has no unwrapping method!");
	}

	/**
	 * Checks if a <b>Position</b> is over/beyond a <b>Bounds</b>.
	 * @param pos The <b>Position</b> to check.
	 * @return {@code true} if the <b>Position</b> is over/beyond the <b>Bounds</b>.
	 */
	public boolean isOver(T pos) {
		throw new UnsupportedOperationException("The \"" + pos.getClass().getSimpleName() + "\" type has no over-bounds checking method!");
	}
}
