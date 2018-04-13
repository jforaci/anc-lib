package org.foraci.anc.cdp;

/**
 * CEA-708 CDP CC service information section element
 *
 * @author Joe Foraci
 */
public class CdpCcServiceInfoElement
{
    private int serviceNumber;

    public CdpCcServiceInfoElement(int serviceNumber, int[] data)
    {
        this.serviceNumber = serviceNumber;
    }

    public int getServiceNumber()
    {
        return serviceNumber;
    }
}
