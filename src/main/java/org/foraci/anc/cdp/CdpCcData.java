package org.foraci.anc.cdp;

import org.foraci.anc.anc.AncTrackReader;
import org.foraci.anc.anc.Smpte291InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * CEA-708 CDP packet CC data section
 *
 * @author Joe Foraci
 */
public class CdpCcData
{
    private static final Logger log = LoggerFactory.getLogger(CdpCcData.class);

    private static long tcount=0;
    private int markers;
    private int count;
    private List elements;

    public CdpCcData(int markers, int count, List elements)
    {
        this.markers = markers;
        this.count = count;
        this.elements = elements;
    }

    public static CdpCcData fromInputStream(Smpte291InputStream in, AncTrackReader context) throws IOException
    {
        int ident1 = in.readWord();
        if (ident1 != 0x72) {
            throw new IOException("bad CDP CC DATA header");
        }
        int word;
        word = in.readWord();
        int markers = ((word >> 5) & 0x7);
        int count = (word & 0x1F);
//        ArrayList elements = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            word = in.readWord();
            int markerBits = ((word >> 3) & 0x1F);
            boolean valid = (((word >> 2) & 0x1) == 1);
            int type = (word & 0x3); //TODO: bit swap for lsb??
            int data1 = in.readWord();
            int data2 = in.readWord();
            if (type > 1) {
                // check if we have to finish off a DTVCC packet
                Object lastWasInDtvCCPacketObj = context.getAttribute("lastWasInDtvCCPacket");
                boolean lastWasInDtvCCPacket = (Boolean.TRUE.equals(lastWasInDtvCCPacketObj));
                if (lastWasInDtvCCPacket && (type == 2 || type == 3) && !valid) {
//                    context.debug("PACKET END!!! " + type + "," + valid);
                    lastWasInDtvCCPacket = false;
                    context.setAttribute("lastWasInDtvCCPacket", Boolean.FALSE);
                }
                if (valid) {
                    if (type == 3) {
                        lastWasInDtvCCPacket = true;
                        context.setAttribute("lastWasInDtvCCPacket", Boolean.TRUE);
//                        context.debug("PACKET START!!! " + type + "," + valid);
                    }
                    if (!lastWasInDtvCCPacket) {
                        //throw new IllegalStateException("DTVCC data without DTVCC header");
                        log.warn("DTVCC data without DTVCC header");
                    }
                    if (context.getCdpOutputStream() != null) {
                        context.getCdpOutputStream().write(data1);
                        context.getCdpOutputStream().write(data2);
//                        context.getCdpOutputStream().flush();
                    }
//                    context.debug("\t\ttype=" + type + ",valid=" + valid + ",data1=" + Integer.toHexString(data1) + ",data2=" + Integer.toHexString(data2));
                }
            } else if (valid) {
                if (type == 0) {
                    if (context.getCC1OutputStream() != null) {
                        context.getCC1OutputStream().write(data1);
                        context.getCC1OutputStream().write(data2);
                    }
                } else if (type == 1) {
                    if (context.getCC3OutputStream() != null) {
                        context.getCC3OutputStream().write(data1);
                        context.getCC3OutputStream().write(data2);
                    }
                }
                System.out.printf(Integer.toString(type));
            }
//            CdpCcDataElement element = new CdpCcDataElement(markerBits, valid, type, data1, data2);
//            elements.add(element);
        }
        System.out.printf(" " + tcount + "\n");
        ++tcount;
        CdpCcData data = new CdpCcData(markers, count, null /*elements*/);
        return data;
    }

    public int getMarkers()
    {
        return markers;
    }

    public int getCount()
    {
        return count;
    }

    public List getElements()
    {
        return elements;
    }
}
