package org.foraci.anc.anc;

import java.io.IOException;

/**
 * <p>An ancillary packet header. This must be created with a valid DID, SDID, and user data word
 * count. Checksum may be set to zero.
 * <p>The <code>read()</code> method is called to read the data of the packet (using the
 * <code>Smpte291InputStream.readWord()</code> method) and return an implementation of
 * <code>AncPacketUserData</code>. This implementation will just call <code>skip()</code>
 * and return <code>null</code>
 *
 * @author Joe Foraci
 */
public class AncPacketHeader
{
    private final AncPacketId id;
    private final int dataCount;
    private int checksum;

    public AncPacketHeader(AncPacketId id, int dataCount)
    {
        this.id = id;
        this.dataCount = dataCount;
        this.checksum = 0;
    }

    public void skip(Smpte291InputStream in) throws IOException
    {
        for (int i = 0; i < getDataCount(); i++) {
            int udw = in.readWord();
        }
    }

    /**
     * Read the ancillary data payload. This implementation returns <code>null</code>.
     * @param in The SMPTE 291 ancilliray input stream
     * @return <code>null</code>; subclasses would return an implementation of
     *         <code>AncPacketUserData</code> for this packet header
     * @throws IOException
     */
    public AncPacketUserData read(Smpte291InputStream in, AncTrackReader context) throws IOException
    {
        // if unknown, just skip
        skip(in);
        return null;
    }

    public AncPacketId getId()
    {
        return id;
    }

    public int getDataCount()
    {
        return dataCount;
    }

    public int getChecksum()
    {
        return checksum;
    }

    public void setChecksum(int checksum)
    {
        this.checksum = checksum;
    }
}
