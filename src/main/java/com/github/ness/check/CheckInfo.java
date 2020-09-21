package com.github.ness.check;

import org.bukkit.event.Event;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Determines how a check should be orchestrated
 *
 * @param <T> ignored parameter, kept for legacy purposes
 * 
 * @author A248
 */
public class CheckInfo<T> {

	/**
	 * Interval of repeating async task, {@link Duration#ZERO} for none
	 */
	private final Duration asyncInterval;

	CheckInfo(Duration asyncInterval) {
		if (asyncInterval.isNegative()) {
			throw new IllegalArgumentException("asyncInterval cannot be negative");
		}
		this.asyncInterval = Objects.requireNonNull(asyncInterval, "asyncInterval");
	}
	
	boolean hasAsyncInterval() {
		return !asyncInterval.isZero();
	}
	
	Duration getAsyncInterval() {
		return asyncInterval;
	}

	/**
	 * Gets a check info demanding a periodic async task
	 * 
	 * @param asyncInterval the interval of the periodic task
	 * @return the check info
	 */
	public static CheckInfo<?> asyncPeriodic(Duration asyncInterval) {
		return new CheckInfo<>(asyncInterval);
	}

	/**
	 * Gets a check info to listen to a certain event <i>and</i> with a periodic
	 * async task
	 * 
	 * @param <E>           the event
	 * @param event         the event class
	 * @param asyncInterval the interval of the periodic task
	 * @return the listening check info
	 */
	public static <E extends Event> ListeningCheckInfo<E> forEventWithAsyncPeriodic(Class<E> event,
			Duration asyncInterval) {
		return new ListeningCheckInfo<>(asyncInterval, event);
	}

	/**
	 * Gets a check info to listen to a certain event
	 * 
	 * @param <E>   the event
	 * @param event the event class
	 * @return the listening check info
	 */
	public static <E extends Event> ListeningCheckInfo<E> forEvent(Class<E> event) {
		return new ListeningCheckInfo<>(event);
	}

	/**
	 * Legacy method for getting a check info to listen to a certain event
	 * 
	 * @param <E>   the event
	 * @param event the event class
	 * @return the check info
	 * @deprecated Prefer {@link #forEvent(Class)} as it is better named
	 */
	@Deprecated
	public static <E extends Event> CheckInfo<E> eventOnly(Class<E> event) {
		return parameteriseForLegacy(forEvent(event));
	}

	/**
	 * Legacy method for getting a check info to listen to a certain event and with a periodic async task
	 * 
	 * @param <E> the event
	 * @param event the event class
	 * @param interval the interval of the periodic task
	 * @param units the units of the interval of the periodic task
	 * @return the listening check info
	 * @deprecated Use {@link #forEventWithAsyncPeriodic(Class, Duration)} which uses a {@code Duration}
	 */
	@Deprecated
	public static <E extends Event> CheckInfo<E> eventWithAsyncPeriodic(Class<E> event, long interval,
			TimeUnit units) {
		Objects.requireNonNull(units, "units");
		return parameteriseForLegacy(forEventWithAsyncPeriodic(event, Duration.of(interval, chronoUnitFromTimeUnit(units))));
	}
	
	@SuppressWarnings("unchecked")
	private static <E extends Event> CheckInfo<E> parameteriseForLegacy(ListeningCheckInfo<E> listeningCheckInfo) {
		CheckInfo<?> info = listeningCheckInfo;
		// Safe because the type parameter in CheckInfo is useless
		return (CheckInfo<E>) info;
	}

	private static ChronoUnit chronoUnitFromTimeUnit(TimeUnit unit) {
		switch (unit) {
		case DAYS:
			return ChronoUnit.DAYS;

		case HOURS:
			return ChronoUnit.HOURS;
		case MINUTES:
			return ChronoUnit.MINUTES;

		case SECONDS:
			return ChronoUnit.SECONDS;
		case MILLISECONDS:
			return ChronoUnit.MILLIS;
		case MICROSECONDS:
			return ChronoUnit.MICROS;
		case NANOSECONDS:
			return ChronoUnit.NANOS;

		default:
			throw new IllegalArgumentException("Unknown TimeUnit " + unit);
		}
	}

}
