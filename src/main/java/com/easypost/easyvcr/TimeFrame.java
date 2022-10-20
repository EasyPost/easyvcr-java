package com.easypost.easyvcr;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * TimeFrame used to store an extent of time.
 */
public class TimeFrame {
    /**
     * Number of days for this time frame.
     */
    private int days = 0;
    /**
     * Number of hours for this time frame.
     */
    private int hours = 0;
    /**
     * Number of minutes for this time frame.
     */
    private int minutes = 0;
    /**
     * Number of seconds for this time frame.
     */
    private int seconds = 0;

    /**
     * If this time frame is a common time frame.
     */
    private CommonTimeFrames commonTimeFrame = null;

    /**
     * Enums for common time frames.
     */
    private enum CommonTimeFrames {
        Forever, Never
    }

    /**
     * Constructor for TimeFrame.
     *
     * @param days    The number of days in the time frame.
     * @param hours   The number of hours in the time frame.
     * @param minutes The number of minutes in the time frame.
     * @param seconds The number of seconds in the time frame.
     */
    public TimeFrame(int days, int hours, int minutes, int seconds) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    /**
     * Constructor for TimeFrame.
     *
     * @param commonTimeFrame The common time frame to use.
     */
    private TimeFrame(CommonTimeFrames commonTimeFrame) {
        this.commonTimeFrame = commonTimeFrame;
    }

    /**
     * Check if this time frame has lapsed from the given time.
     *
     * @param fromTime Time to add time frame to.
     * @return Whether this time frame has lapsed.
     */
    public boolean hasLapsed(Instant fromTime) {
        Instant startTimePlusFrame = timePlusFrame(fromTime);
        return startTimePlusFrame.isBefore(Instant.now());
    }

    /**
     * Check if this time frame has lapsed from the given time.
     *
     * @param fromTimeEpochTimestamp Epoch timestamp of the time to add time frame to.
     * @return Whether this time frame has lapsed.
     */
    public boolean hasLapsed(long fromTimeEpochTimestamp) {
        Instant fromTime = Instant.ofEpochSecond(fromTimeEpochTimestamp);
        return hasLapsed(fromTime);
    }

    /**
     * Get the provided time plus the time frame.
     *
     * @param fromTime Starting time.
     * @return Starting time plus this time frame.
     */
    private Instant timePlusFrame(Instant fromTime) {
        // We need to do a null check here. The "default" case in the switch statement below doesn't handle null enums.
        if (commonTimeFrame == null) {  // No common time frame was used
            return fromTime.plus(days, ChronoUnit.DAYS).plus(hours, ChronoUnit.HOURS).plus(minutes, ChronoUnit.MINUTES)
                    .plus(seconds, ChronoUnit.SECONDS);
        }
        switch (commonTimeFrame) {
            case Forever:
                return Instant.MAX;  // will always been in the future
            case Never:
                return Instant.MIN; // will always been in the past
            default:
                // We should never get here, since there should always either be an accounted-for enum,
                // or a null value (handled above).
                return fromTime.plus(days, ChronoUnit.DAYS).plus(hours, ChronoUnit.HOURS)
                        .plus(minutes, ChronoUnit.MINUTES).plus(seconds, ChronoUnit.SECONDS);
        }
    }

    /**
     * Get a TimeFrame that represents "forever".
     *
     * @return TimeFrame that represents "forever".
     */
    public static TimeFrame forever() {
        return new TimeFrame(CommonTimeFrames.Forever);
    }

    /**
     * Get a TimeFrame that represents "never".
     *
     * @return TimeFrame that represents "never".
     */
    public static TimeFrame never() {
        return new TimeFrame(CommonTimeFrames.Never);
    }

    /**
     * Get a TimeFrame that represents 1 month.
     *
     * @return TimeFrame that represents 1 month.
     */
    public static TimeFrame months1() {
        return new TimeFrame(30, 0, 0, 0);
    }

    /**
     * Get a TimeFrame that represents 2 months.
     *
     * @return TimeFrame that represents 2 months.
     */
    public static TimeFrame months2() {
        return new TimeFrame(61, 0, 0, 0);
    }

    /**
     * Get a TimeFrame that represents 3 months.
     *
     * @return TimeFrame that represents 3 months.
     */
    public static TimeFrame months3() {
        return new TimeFrame(91, 0, 0, 0);
    }

    /**
     * Get a TimeFrame that represents 6 months.
     *
     * @return TimeFrame that represents 6 months.
     */
    public static TimeFrame months6() {
        return new TimeFrame(182, 0, 0, 0);
    }

    /**
     * Get a TimeFrame that represents 12 months.
     *
     * @return TimeFrame that represents 12 months.
     */
    public static TimeFrame months12() {
        return new TimeFrame(365, 0, 0, 0);
    }
}
