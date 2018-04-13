package org.foraci.anc.cdp;

import org.foraci.anc.anc.AncPacketUserData;

/**
 * Represents a CEA-708 Caption Data packet (CDP) ancillary packet
 *
 * @author Joe Foraci
 */
public class CdpPacket implements AncPacketUserData
{
    private CdpHeader cdpHeader;
    private CdpTimecode cdpTimecode;
    private CdpCcData cdpCcData;
    private CdpCcServiceInfo cdpCcServiceInfo;
    private CdpFooter cdpFooter;

    public CdpPacket(CdpHeader cdpHeader, CdpTimecode cdpTimecode, CdpCcData cdpCcData, CdpCcServiceInfo cdpCcServiceInfo, CdpFooter cdpFooter)
    {
        this.cdpHeader = cdpHeader;
        this.cdpTimecode = cdpTimecode;
        this.cdpCcData = cdpCcData;
        this.cdpCcServiceInfo = cdpCcServiceInfo;
        this.cdpFooter = cdpFooter;
    }

    public CdpHeader getCdpHeader()
    {
        return cdpHeader;
    }

    public CdpTimecode getCdpTimecode()
    {
        return cdpTimecode;
    }

    public CdpCcData getCdpCcData()
    {
        return cdpCcData;
    }

    public CdpCcServiceInfo getCdpCcServiceInfo()
    {
        return cdpCcServiceInfo;
    }

    public CdpFooter getCdpFooter()
    {
        return cdpFooter;
    }
}
