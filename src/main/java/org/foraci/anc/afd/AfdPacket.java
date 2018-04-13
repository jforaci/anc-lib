package org.foraci.anc.afd;

import org.foraci.anc.anc.AncPacketUserData;

/**
 * An packet containing Active Format Description (AFD) and Bar data
 */
public class AfdPacket implements AncPacketUserData
{
    private final boolean aspectFlag;
    private final int code;
    private final int barFlags;

    public AfdPacket(boolean aspectFlag, int code, int barFlags)
    {
        this.aspectFlag = aspectFlag;
        this.code = code;
        this.barFlags = barFlags;
    }

    public int getCode() {
        return code;
    }

    public String getDescription()
    {
        String aspect = (aspectFlag) ? "16:9" : "4:3";
        return String.format("aspect: %s, AFD code=0x%x, BAR flags=0x%x",
                aspect, code, barFlags);
    }
}
