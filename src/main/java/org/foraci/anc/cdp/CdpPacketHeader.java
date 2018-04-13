package org.foraci.anc.cdp;

import org.foraci.anc.anc.*;

import java.io.IOException;

/**
 * An implementation of {@link AncPacketHeader} that reads a CEA-708 Caption Data Packet (CDP)
 *
 * @author Joe Foraci
 */
public class CdpPacketHeader extends AncPacketHeader
{
    public CdpPacketHeader(int dataCount)
    {
        super(new AncPacketId(0x61, 0x01), dataCount);
    }

    @Override
    public AncPacketUserData read(Smpte291InputStream in, AncTrackReader context) throws IOException
    {
        CdpHeader cdpHeader = CdpHeader.fromInputStream(in);
        CdpTimecode cdpTimecode = null;
        if (cdpHeader.isTcPresent()) {
            cdpTimecode = CdpTimecode.fromInputStream(in);
        }
        CdpCcData cdpCcData = null;
        if (cdpHeader.isCcPresent()) {
            cdpCcData = CdpCcData.fromInputStream(in, context);
        }
        CdpCcServiceInfo cdpCcServiceInfo = null;
        if (cdpHeader.isServiceInfoPresent()) {
            cdpCcServiceInfo = CdpCcServiceInfo.fromInputStream(in, cdpCcData);
        }
        CdpFooter cdpFooter = CdpFooter.fromInputStream(in);
        CdpPacket packet = new CdpPacket(cdpHeader, cdpTimecode, cdpCcData, cdpCcServiceInfo, cdpFooter);
        return packet;
    }
}
