package org.foraci.anc.util.timecode;

/**
 * Schemes associating edit units to a rate
 * @author Joe Foraci
 */
public enum TimecodeBase {
    NTSC(30, 30000, 1001, true),
    PAL(25, 25, 1, false),
    FILM(24, 24000, 1001, false);

    public static TimecodeBase forParams(int roundedTimecodeBase, boolean dropFrame) {
        if (roundedTimecodeBase == 30 && dropFrame) {
            return NTSC;
        } else if (!dropFrame) {
            switch (roundedTimecodeBase) {
                case 24:
                    return FILM;
                case 25:
                    return PAL;
                default:
                    throw new IllegalArgumentException(
                            String.format("invalid timecode parameters: rounded base of %d (non-drop)", roundedTimecodeBase));
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("invalid timecode parameters: rounded base of %d (drop)", roundedTimecodeBase));
        }
    }

    private final int roundedTimecodeBase;
    private final int editUnitsPerInterval;
    private final int secondsPerInterval;
    private final boolean dropFrame;

    TimecodeBase(int roundedTimecodeBase, int editUnitsPerInterval, int secondsPerInterval, boolean dropFrame) {
        this.roundedTimecodeBase = roundedTimecodeBase;
        this.editUnitsPerInterval = editUnitsPerInterval;
        this.secondsPerInterval = secondsPerInterval;
        this.dropFrame = dropFrame;
    }

    public int getRoundedTimecodeBase() {
        return roundedTimecodeBase;
    }

    public int getEditUnitsPerInterval() {
        return editUnitsPerInterval;
    }

    public int getSecondsPerInterval() {
        return secondsPerInterval;
    }

    public boolean isDropFrame() {
        return dropFrame;
    }
}
