package org.foraci.anc.anc;

/**
 * An immutable class, whose instance holds the DID (Data ID) and
 * SDID (Secondary Data ID) or DBN (Data Block Number) of a packet.
 */
public class AncPacketId
{
    private final int did;
    private final int sdid;

    public AncPacketId(int did, int sdid)
    {
        this.did = did;
        this.sdid = sdid;
    }

    public int getDid()
    {
        return did;
    }

    public int getSdid()
    {
        return sdid;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AncPacketId)) {
            return false;
        }
        AncPacketId other = (AncPacketId) obj;
        return (did == other.did && sdid == other.sdid);
    }

    @Override
    public int hashCode()
    {
        int result = did;
        result = 31 * result + sdid;
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("did=0x%02x,sdid=0x%02x", did, sdid);
    }
}
