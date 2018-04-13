package org.foraci.anc.cdp;

import java.io.ByteArrayOutputStream;

/**
 * CEA-708 CDP data element section
 *
 * @author Joe Foraci
 */
public class CdpCcDataElement
{
    private int marker;
    private boolean valid;
    private int type;
    private int data1;
    private int data2;

    public CdpCcDataElement(int marker, boolean valid, int type, int data1, int data2)
    {
        this.marker = marker;
        this.valid = valid;
        this.type = type;
        this.data1 = data1;
        this.data2 = data2;
    }

    public int getMarker()
    {
        return marker;
    }

    public boolean isValid()
    {
        return valid;
    }

    public int getType()
    {
        return type;
    }

    public void append(ByteArrayOutputStream out1, ByteArrayOutputStream out2)
    {
        
    }
}
