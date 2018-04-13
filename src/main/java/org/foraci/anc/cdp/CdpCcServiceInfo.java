package org.foraci.anc.cdp;

import org.foraci.anc.anc.Smpte291InputStream;

import java.util.ArrayList;
import java.io.IOException;

/**
 * CEA-708 CDP service information section
 *
 * @author Joe Foraci
 */
public class CdpCcServiceInfo
{
    private boolean start;
    private boolean change;
    private boolean complete;
    private int count;
    private ArrayList elements;

    public CdpCcServiceInfo(boolean start, boolean change, boolean complete, int count, ArrayList elements)
    {
        this.start = start;
        this.change = change;
        this.complete = complete;
        this.count = count;
        this.elements = elements;
    }

    public static CdpCcServiceInfo fromInputStream(Smpte291InputStream in, CdpCcData cdpCcData) throws IOException
    {
        int ident1 = in.readWord();
        if (ident1 != 0x73) {
            throw new IOException("bad CDP CC SERVICE INFO header");
        }
        int word;
        word = in.readWord();
        boolean start = (((word >> 6) & 0x1) == 1);
        boolean change = (((word >> 5) & 0x1) == 1);
        boolean complete = (((word >> 4) & 0x1) == 1);
        int count = (word & 0xF);
//        ArrayList elements = new ArrayList(count);
        for (int i = 0; i < count; i++) {
            word = in.readWord();
            boolean sz = (((word >> 6) & 0x1) == 1);
            int serviceNumber;
            if (sz) {
                serviceNumber = (word & 0x1F);
            } else {
                serviceNumber = (word & 0x3F);
            }
            int[] data = new int[6];
            data[0] = in.readWord();
            data[1] = in.readWord();
            data[2] = in.readWord();
            data[3] = in.readWord();
            data[4] = in.readWord();
            data[5] = in.readWord();
            boolean found = true;
            if (cdpCcData != null) {
//                for (Iterator j = cdpCcData.getElements().iterator(); j.hasNext();) {
//                    CdpCcDataElement ccdata = (CdpCcDataElement) j.next();
//                    if (ccdata.getType() > 1 && ccdata.isValid()) {
//                        found = true;
//                        break;
//                    }
//                }
            }
//            in.getContext().debug("serviceNumber=" + serviceNumber);
            if (found) {
                for (int j = 0; j < data.length; j++) {
//                    in.getContext().debug(Integer.toHexString(data[j]));
                }
            }
//            CdpCcServiceInfoElement element = new CdpCcServiceInfoElement(serviceNumber, data);
//            elements.add(element);
        }
        CdpCcServiceInfo data = new CdpCcServiceInfo(start, change, complete, count, null /*elements*/);
        return data;
    }

    public boolean isStart()
    {
        return start;
    }

    public boolean isChange()
    {
        return change;
    }

    public boolean isComplete()
    {
        return complete;
    }

    public int getCount()
    {
        return count;
    }

    public ArrayList getElements()
    {
        return elements;
    }
}
