package org.foraci.anc.cdp;

import org.foraci.anc.anc.Smpte291InputStream;

import java.io.IOException;

/**
 * CEA-708 CDP timecode section
 *
 * @author Joe Foraci
 */
public class CdpTimecode
{
    private int hours;
    private int minutes;
    private int seconds;
    private int frames;
    private int interlaceFlag;
    private boolean dropframe;

    public CdpTimecode(int hours, int minutes, int seconds, int frames, int interlaceFlag, boolean dropframe)
    {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.frames = frames;
        this.interlaceFlag = interlaceFlag;
        this.dropframe = dropframe;
    }

    public static CdpTimecode fromInputStream(Smpte291InputStream in) throws IOException
    {
        int ident1 = in.readWord();
        if (ident1 != 0x71) {
            throw new IOException("bad CDP TC header");
        }
        int temp;
        temp = in.readWord();
        int hours = ((temp >> 4) & 0x3) * 10;
        hours += (temp & 0xF);
        temp = in.readWord();
        int mins = ((temp >> 4) & 0x7) * 10;
        mins += (temp & 0xF);
        temp = in.readWord();
        int secs = ((temp >> 4) & 0x7) * 10;
        secs += (temp & 0xF);
        int interlaceFlag = ((temp >> 7) & 0x1);
        temp = in.readWord();
        boolean dropframe = ((temp >> 7) & 0x1) == 1;
        int frames = ((temp >> 4) & 0x3) * 10;
        frames += (temp & 0xF);
        CdpTimecode cdpTimecode = new CdpTimecode(hours, mins, secs, frames, interlaceFlag, dropframe);
        return cdpTimecode;
    }

    public int getHours()
    {
        return hours;
    }

    public int getMinutes()
    {
        return minutes;
    }

    public int getSeconds()
    {
        return seconds;
    }

    public int getFrames()
    {
        return frames;
    }

    public int getInterlaceFlag()
    {
        return interlaceFlag;
    }

    public boolean isDropframe()
    {
        return dropframe;
    }
}
