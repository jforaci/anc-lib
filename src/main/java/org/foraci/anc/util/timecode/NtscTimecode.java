package org.foraci.anc.util.timecode;

/**
 * NTSC implementation of <code>Timecode</code>
 * @see org.foraci.anc.util.timecode.Timecode
 * @author Joe Foraci
 */
class NtscTimecode extends Timecode {
    private final NtscConverter converter;

    static Timecode fromTimecode(int hours, int minutes, int seconds, int frames) {
        NtscConverter converter = new NtscConverter();
        int offset = converter.convertToFrames(hours, minutes, seconds, frames, false) / 2;
        return new NtscTimecode(offset, offset);
    }

    NtscTimecode(long offset, long position) {
        super(TimecodeBase.NTSC, offset, position);
        converter = new NtscConverter();
    }

    @Override
    public TimecodeLabel getLabel() {
        return TimecodeLabel.valueOf(converter.convertFromFrames((int) getPosition() * 2, false));
    }
}
