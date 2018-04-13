package org.foraci.anc.cdp;

import org.foraci.anc.anc.Smpte291InputStream;

import java.io.IOException;

/**
 * CEA-708 CDP header section
 *
 * @author Joe Foraci
 */
public class CdpHeader
{
    private int length;
    private byte frameRate;
    private byte reserved;
    private boolean tcPresent;
    private boolean ccPresent;
    private boolean serviceInfoPresent;
    private boolean serviceInfoStart;
    private boolean serviceInfoChange;
    private boolean serviceInfoComplete;
    private boolean captionServiceActive;
    private boolean reserved2;
    private int seqCounter;

    public CdpHeader(int length, byte frameRate, byte reserved, boolean tcPresent, boolean ccPresent,
                     boolean serviceInfoPresent, boolean serviceInfoStart, boolean serviceInfoChange,
                     boolean serviceInfoComplete, boolean captionServiceActive, boolean reserved2, int seqCounter)
    {

        this.length = length;
        this.frameRate = frameRate;
        this.reserved = reserved;
        this.tcPresent = tcPresent;
        this.ccPresent = ccPresent;
        this.serviceInfoPresent = serviceInfoPresent;
        this.serviceInfoStart = serviceInfoStart;
        this.serviceInfoChange = serviceInfoChange;
        this.serviceInfoComplete = serviceInfoComplete;
        this.captionServiceActive = captionServiceActive;
        this.reserved2 = reserved2;
        this.seqCounter = seqCounter;
    }

    public static CdpHeader fromInputStream(Smpte291InputStream in) throws IOException
    {
        int ident1 = in.readWord();
        int ident2 = in.readWord();
        if (ident1 != 0x96 || ident2 != 0x69) {
            throw new IOException("bad CDP header");
        }
        int temp;
        int length = in.readWord();
        temp = in.readWord();
        byte frameRate = (byte) ((temp >> 4) & 0xF);
        if (frameRate == 0 || frameRate > 8) {
            throw new IOException("forbidden/reserved frameRate specified: " + frameRate);
        }
        byte reserved1 = (byte) (temp & 0xF);
        temp = in.readWord();
        boolean tcPresent = ((temp & 0x80) != 0);
        boolean ccDataPresent = ((temp & 0x40) != 0);
        boolean serviceInfoPresent = ((temp & 0x20) != 0);
        boolean serviceInfoStart = ((temp & 0x10) != 0);
        boolean serviceInfoChange = ((temp & 0x08) != 0);
        boolean serviceInfoComplete = ((temp & 0x04) != 0);
        boolean captionServiceActive = ((temp & 0x02) != 0);
        boolean reserved2 = ((temp & 0x01) != 0);
        int sequenceCounter = (((in.readWord() & 0xff) << 8) | (in.readWord() & 0xff));
        return new CdpHeader(length, frameRate, reserved1, tcPresent, ccDataPresent,
                serviceInfoPresent, serviceInfoStart, serviceInfoChange, serviceInfoComplete,
                captionServiceActive, reserved2, sequenceCounter);
    }

    public int getLength()
    {
        return length;
    }

    public byte getFrameRate()
    {
        return frameRate;
    }

    public byte getReserved()
    {
        return reserved;
    }

    public boolean isTcPresent()
    {
        return tcPresent;
    }

    public boolean isCcPresent()
    {
        return ccPresent;
    }

    public boolean isServiceInfoPresent()
    {
        return serviceInfoPresent;
    }

    public boolean isServiceInfoStart()
    {
        return serviceInfoStart;
    }

    public boolean isServiceInfoChange()
    {
        return serviceInfoChange;
    }

    public boolean isServiceInfoComplete()
    {
        return serviceInfoComplete;
    }

    public boolean isCaptionServiceActive()
    {
        return captionServiceActive;
    }

    public boolean isReserved2()
    {
        return reserved2;
    }

    public int getSeqCounter()
    {
        return seqCounter;
    }
}
