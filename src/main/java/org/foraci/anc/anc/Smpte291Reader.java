package org.foraci.anc.anc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Drives the reading of a <code>Smpte291InputStream</code>
 *
 * @author Joe Foraci
 */
public class Smpte291Reader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(Smpte291Reader.class);

    private Smpte291InputStream in;
    private final TrackAttributesCollection trackAttributesCollection;

    public Smpte291Reader(Smpte291InputStream in) throws IOException
    {
        this.in = in;
        this.trackAttributesCollection = new TrackAttributesCollection();
    }

    void addTrackAttributes(TrackAttributes trackAttributes)
    {
        trackAttributesCollection.addTrackAttributes(trackAttributes);
    }

    public void run()
    {
        try {
            do {
                long pos = in.getPosition();
                AncPacketHeader header = in.readAncPacket();
                AncPacketUserData data = in.readAncPacketUserData(header);
                TrackAttributes currentTrackAttributes = trackAttributesCollection.getTrackAttributes(pos);
                in.getContext().fireAncPacketEvent(header, data, currentTrackAttributes);
                in.getContext().debug("pyld: " + header.getId());
            } while (true);
        } catch (EOFException eof) {
            log.info("END of SMPTE 291 stream");
        } catch (IOException e) {
            log.error("SMPTE 291 IOException", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                if (in.getContext().getCdpOutputStream() != null) {
                    in.getContext().getCdpOutputStream().flush();
                    in.getContext().getCdpOutputStream().close();
                }
                if (in.getContext().getCC1OutputStream() != null) {
                    in.getContext().getCC1OutputStream().flush();
                    in.getContext().getCC1OutputStream().close();
                }
                if (in.getContext().getCC3OutputStream() != null) {
                    in.getContext().getCC3OutputStream().flush();
                    in.getContext().getCC3OutputStream().close();
                }
            } catch (IOException ioe) {
                log.error("failed closing caption output streams", ioe);
            }
        }
    }
}
