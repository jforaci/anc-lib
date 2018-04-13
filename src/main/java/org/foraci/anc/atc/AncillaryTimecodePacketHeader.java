package org.foraci.anc.atc;

import org.foraci.anc.anc.*;

import java.io.IOException;

/**
 * An implementation of <code>AncPacketHeader</code> that reads ancillary timecode (ATC).
 * See SMPTE 12m-2.
 */
public class AncillaryTimecodePacketHeader extends AncPacketHeader
{
    public AncillaryTimecodePacketHeader(int dataCount)
    {
        super(new AncPacketId(0x60, 0x60), dataCount);
    }

    @Override
    public AncPacketUserData read(Smpte291InputStream in, AncTrackReader context) throws IOException
    {
        int framesUnits = (in.readWord() >> 4) & 0xF;
        in.readWord(); // binary group 1
        int udw3 = in.readWord();
        int framesTens = ((udw3 >> 4) & 0x3) * 10;
        boolean dropFrame = (udw3 & 0x40) == 0x40;
        boolean colorFrame = (udw3 & 0x80) == 0x80;
        in.readWord(); // binary group 2
        int secondsUnits = (in.readWord() >> 4) & 0xF;
        in.readWord(); // binary group 3
        int udw7 = in.readWord();
        //boolean frameIdentFlag = (udw7 & 0x80) == 0x80;
        int secondsTens = ((udw7 >> 4) & 0x7) * 10;
        in.readWord(); // binary group 4
        int minutesUnits = (in.readWord() >> 4) & 0xF;
        in.readWord(); // binary group 5
        int minutesTens = ((in.readWord() >> 4) & 0x7) * 10;
        in.readWord(); // binary group 6
        int hoursUnits = (in.readWord() >> 4) & 0xF;
        in.readWord(); // binary group 7
        int hoursTens = ((in.readWord() >> 4) & 0x3) * 10;
        in.readWord(); // binary group 8
        return new AncillaryTimecodePacket(
                hoursTens + hoursUnits, minutesTens + minutesUnits,
                secondsTens + secondsUnits, framesTens + framesUnits,
                dropFrame, colorFrame);
    }
}
