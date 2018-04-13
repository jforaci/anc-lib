package org.foraci.anc.util.timecode;

import java.util.Arrays;

/**
 * A timecode label consisting of hours, minutes, seconds and frames (edit units)
 * @author Joe Foraci
 */
public class TimecodeLabel {
    public static TimecodeLabel valueOf(String timecodeLabelString) {
        final int labelLength = 11;
        if (timecodeLabelString.length() != labelLength) {
            throw new IllegalArgumentException("timecode label length must be " + labelLength);
        }
        final char fieldSeparatorChar = ':';
        if (timecodeLabelString.charAt(2) != fieldSeparatorChar
                || timecodeLabelString.charAt(5) != fieldSeparatorChar) {
            throw new IllegalArgumentException("timecode label has an invalid field separator");
        }
        final char[] frameSeparatorChars = { '.', ':', ';'};
        if (Arrays.binarySearch(frameSeparatorChars, timecodeLabelString.charAt(8)) < 0) {
            throw new IllegalArgumentException("timecode label has an invalid frame separator");
        }
        return new TimecodeLabel(Integer.parseInt(timecodeLabelString.substring(0, 2)),
                Integer.parseInt(timecodeLabelString.substring(3, 5)),
                Integer.parseInt(timecodeLabelString.substring(6, 8)),
                Integer.parseInt(timecodeLabelString.substring(9, 11)));
    }

    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int frames;

    public TimecodeLabel(int hours, int minutes, int seconds, int frames) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.frames = frames;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getFrames() {
        return frames;
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d:%02d:%02d", hours, minutes, seconds, frames);
    }
}
