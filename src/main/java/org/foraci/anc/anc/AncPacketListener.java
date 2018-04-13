package org.foraci.anc.anc;

/**
 * Receives packets in an ancillary/VBI stream
 */
public interface AncPacketListener
{
    void packet(AncPacketHeader header, AncPacketUserData payload, TrackAttributes trackAttributes);
}
