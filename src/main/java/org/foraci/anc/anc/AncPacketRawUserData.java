package org.foraci.anc.anc;

/**
 * A tagging interface to identify an SMPTE 291 ancillary packet payload
 *
 * @author Joe Foraci
 */
public class AncPacketRawUserData implements AncPacketUserData
{
    private final int[] dataWords;

    public AncPacketRawUserData(int[] dataWords)
    {
        this.dataWords = dataWords;
    }

    public int[] getDataWords()
    {
        return dataWords;
    }
}
