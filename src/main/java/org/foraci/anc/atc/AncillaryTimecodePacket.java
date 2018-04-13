package org.foraci.anc.atc;

import org.foraci.anc.anc.AncPacketUserData;

/**
 * An ancillary timecode (ATC) packet
 */
public class AncillaryTimecodePacket implements AncPacketUserData
{
    private final int hours;
    private final int minutes;
    private final int seconds;
    private final int frames;
    private final boolean dropFrame;
    private final boolean colorFrame;

    public AncillaryTimecodePacket(int hours, int minutes, int seconds, int frames,
                                   boolean dropFrame, boolean colorFrame)
    {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.frames = frames;
        this.dropFrame = dropFrame;
        this.colorFrame = colorFrame;
    }

    public String getTimecode()
    {
        return String.format("%02d:%02d:%02d:%02d DF=%b CF=%b",
                hours, minutes, seconds, frames, dropFrame, colorFrame);
    }
}
