package org.foraci.anc.cdp;

import org.foraci.anc.anc.Smpte291InputStream;

import java.io.IOException;

/**
 * CEA-708 CDP footer section
 *
 * @author Joe Foraci
 */
public class CdpFooter
{
    private int sequenceCounter;
    private int checksum;

    public CdpFooter(int sequenceCounter, int checksum)
    {
        this.sequenceCounter = sequenceCounter;
        this.checksum = checksum;
    }

    public static CdpFooter fromInputStream(Smpte291InputStream in) throws IOException
    {
        int ident1 = in.readWord();
        if (ident1 != 0x74) {
            throw new IOException("bad CDP footer");
        }
        int sequenceCounter = (((in.readWord() & 0xff) << 8) | (in.readWord() & 0xff));
        int checksum = in.readWord();
        return new CdpFooter(sequenceCounter, checksum);
    }

    public int getSequenceCounter()
    {
        return sequenceCounter;
    }

    public int getChecksum()
    {
        return checksum;
    }
}
