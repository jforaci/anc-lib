package org.foraci.anc;

import org.foraci.anc.cdp.CdpReader;
import org.foraci.anc.anc.AncTrackReader;
import org.foraci.anc.anc.Smpte291InputStream;

import java.io.*;

/**
 * Test to read extracted CDP stream
 *
 * @author Joe Foraci
 */
public class CdpTest extends AncTrackReader
{
    public static void main(String[] args) throws IOException, InterruptedException
    {
        CdpTest context = new CdpTest();
        context.setDebug(true);
        Thread cdpReaderThread = new Thread(new CdpReader(new FileInputStream(new File(args[0])), context), "CDP-READER");
        cdpReaderThread.start();
        cdpReaderThread.join();
    }

    private CdpTest() throws IOException
    {
        super(null);
    }

    protected Smpte291InputStream createSmpte291InputStream(InputStream inputStream)
    {
        return null;  //TODO: implement
    }

    protected void doRead(OutputStream ancillaryOutputStream) throws IOException
    {
        //TODO: implement
    }
}
