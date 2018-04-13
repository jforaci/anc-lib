package org.foraci.anc.anc;

import org.foraci.anc.afd.AfdPacketHeader;
import org.foraci.anc.atc.AncillaryTimecodePacketHeader;
import org.foraci.anc.cdp.CdpPacketHeader;
import org.foraci.anc.util.io.CountingInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p>An input stream provider for SMPTE 291 ancillary data. When reading is started by the
 * <code>AncTrackReader</code>, the <code>readAncPacket()</code>, <code>readAncPacketUserData()</code>,
 * and <code>skipAncPacket()</code> methods will be called by the framework to read ancillary
 * packets. In turn, these all call <code>readWord()</code> to be able to properly read a single
 * sample word from the input stream.
 * <p>The default implementation provides <code>createAncPacketHeader()</code> to create
 * instances of <code>AncPacketHeader</code> that know how to parse themselves from the input
 * stream. Right now only a CDP (708) implementation is provided. By default, a packet header
 * will just skip its data when <code>AncPacketHeader.read()</code> is called.
 *
 * @author Joe Foraci
 */
public abstract class Smpte291InputStream
{
    protected InputStream in;
    protected CountingInputStream cin;
    protected AncTrackReader context;

    public Smpte291InputStream(InputStream in, AncTrackReader context)
    {
        this.cin = new CountingInputStream(in);
        this.in = cin;
        this.context = context;
    }

    public AncTrackReader getContext()
    {
        return context;
    }

    public long getPosition()
    {
        return cin.getPosition();
    }

    public abstract int readWord() throws IOException;

    public abstract AncPacketHeader readAncPacket() throws IOException;

    public abstract void skipAncPacket(AncPacketHeader header) throws IOException;
    
    public abstract AncPacketUserData readAncPacketUserData(AncPacketHeader header) throws IOException;

    public abstract AncPacketRawUserData readAncPacketRawUserData(AncPacketHeader header) throws IOException;

    public AncPacketHeader createAncPacketHeader(int did, int sdid, int dataCount)
    {
        if (did == 0x61 && sdid == 0x01) {
            return new CdpPacketHeader(dataCount);
        } else if (did == 0x60 && sdid == 0x60) {
            return new AncillaryTimecodePacketHeader(dataCount);
        } else if (did == 0x41 && sdid == 0x05) {
            return new AfdPacketHeader(dataCount);
        }
        AncPacketHeader ancPacketHeader = new AncPacketHeader(new AncPacketId(did, sdid), dataCount);
        return ancPacketHeader;
    }
}
