package org.foraci.anc.util.timecode;

/**
 * A representation of a position based on edit units,
 * that can be translated to and from a timecode
 * @author Joe Foraci
 */
public class Timecode {
    private final TimecodeBase timecodeBase;
    private final long offset;
    private long position;

    public static Timecode fromEditUnits(TimecodeBase timecodeBase, long offset) {
        return fromEditUnits(timecodeBase, offset, offset);
    }

    public static Timecode fromEditUnits(TimecodeBase timecodeBase, long offset, long position) {
        if (TimecodeBase.NTSC.equals(timecodeBase)) {
            return new NtscTimecode(offset, position);
        } else {
            return new Timecode(timecodeBase, offset, position);
        }
    }

    public static Timecode fromTimecode(TimecodeBase timecodeBase,
                                        TimecodeLabel timecodeLabel) {
        return fromTimecode(timecodeBase, timecodeLabel.getHours(), timecodeLabel.getMinutes(),
                timecodeLabel.getSeconds(), timecodeLabel.getFrames());
    }

    public static Timecode fromTimecode(TimecodeBase timecodeBase,
                                        int hours, int minutes, int seconds, int frames) {
        final long offset;
        if (TimecodeBase.NTSC.equals(timecodeBase)) {
            return NtscTimecode.fromTimecode(hours, minutes, seconds, frames);
        } else {
            offset = ((hours * 3600) + (minutes * 60) + seconds)
                    * timecodeBase.getRoundedTimecodeBase() + frames;
            return new Timecode(timecodeBase, offset, offset);
        }
    }

    protected Timecode(TimecodeBase timecodeBase, long offset, long position) {
        this.timecodeBase = timecodeBase;
        this.offset = offset;
        this.position = position;
    }

    public TimecodeBase getTimecodeBase() {
        return timecodeBase;
    }

    public long getOffset() {
        return offset;
    }

    public long getPosition() {
        return position;
    }

    public void increment() {
        ++position;
    }

    public Timecode to(TimecodeBase newTimecodeBase) {
        if (timecodeBase.equals(newTimecodeBase)) {
            // no conversion necessary
            return this;
        }
        final long newOffset, newPosition;
        if (timecodeBase.getSecondsPerInterval() == newTimecodeBase.getSecondsPerInterval()) {
            // factor out common interval before calculating the new position
            // in the new timecode base
            newOffset = (offset * newTimecodeBase.getEditUnitsPerInterval())
                    / timecodeBase.getEditUnitsPerInterval();
            newPosition = (offset * newTimecodeBase.getEditUnitsPerInterval())
                    / timecodeBase.getEditUnitsPerInterval();
        } else {
            // calculate the new position in the new timecode base
            long numerator = (newTimecodeBase.getEditUnitsPerInterval()
                    * timecodeBase.getSecondsPerInterval());
            long denominator = (newTimecodeBase.getSecondsPerInterval()
                    * timecodeBase.getEditUnitsPerInterval());
            newOffset = (offset * numerator) / denominator;
            newPosition = (offset * numerator) / denominator;
        }
        return fromEditUnits(newTimecodeBase, newOffset, newPosition);
    }

    public TimecodeLabel getLabel() {
        int roundedTimecodeBase = timecodeBase.getRoundedTimecodeBase();
        long hours = position / (3600 * roundedTimecodeBase);
        long minutes = (position / (60 * roundedTimecodeBase)) % 60;
        long seconds = (position / roundedTimecodeBase) % 60;
        long fractionalSecondInEditUnits = position % roundedTimecodeBase;
        return new TimecodeLabel((int)hours, (int)minutes, (int)seconds, (int)fractionalSecondInEditUnits);
    }

    @Override
    public String toString() {
        return getLabel().toString();
    }
}
