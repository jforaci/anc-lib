package org.foraci.anc.afd;

import org.foraci.anc.anc.*;

import java.io.IOException;

/**
 * An implementation of {@link AncPacketHeader} that reads
 * Active Format Description (AFD) and Bar data
 * See SMPTE 2016-1 and 2016-3
 */
public class AfdPacketHeader extends AncPacketHeader
{
    public AfdPacketHeader(int dataCount)
    {
        super(new AncPacketId(0x41, 0x05), dataCount);
    }

    @Override
    public AncPacketUserData read(Smpte291InputStream in, AncTrackReader context) throws IOException
    {
        final int packetLength = 8;
        if (getDataCount() != packetLength) {
            throw new RuntimeException("dataCount is " + getDataCount() + "; should be " + packetLength);
        }
        int afdWord = in.readWord();
        boolean aspectFlag = ((afdWord & 0x4) != 0);
        int code = (afdWord >> 3) & 0xF;
        in.readWord(); // reserved word 1
        in.readWord(); // reserved word 2
        int barFlags = (in.readWord() >> 4) & 0xF;
        // TODO: fix -- we throw away the rest for now...
        for (int i = 0; i < 4; i++) {
            in.readWord();
        }
        return new AfdPacket(aspectFlag, code, barFlags);
    }
}
