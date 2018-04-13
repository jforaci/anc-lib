package org.foraci.anc.anc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Uses a <code>Smpte291InputStream</code> to write a SMPTE 436M data track formatted file
 */
public class Smpte436Writer implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(Smpte436Writer.class);

    private Smpte291InputStream in;
    private final DataOutputStream smpte436MOutputStream;
    private final TrackAttributesCollection trackAttributesCollection;
    private ByteArrayOutputStream sampleOutputStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream packetBuffer = new ByteArrayOutputStream();
    private DataOutputStream packetOutputStream = new DataOutputStream(packetBuffer);
    private TrackAttributes trackAttributes = null;
    private int sampleCount = 0;
    private int packetCount = 0;
    private int lastField = 0;

    public Smpte436Writer(Smpte291InputStream in, DataOutputStream smpte436MOutputStream)
    {
        this.in = in;
        this.smpte436MOutputStream = smpte436MOutputStream;
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
                AncPacketRawUserData data = in.readAncPacketRawUserData(header);
                TrackAttributes currentTrackAttributes = trackAttributesCollection.getTrackAttributes(pos);
                in.getContext().debug("pyld: " + header.getId());
                int currentField = 0;
                if (currentTrackAttributes != null) {
                    currentField = currentTrackAttributes.getField();
                }
                boolean closeElement = (currentField < lastField);
                lastField = currentField;
                boolean closePacket = (trackAttributes != null && trackAttributes != currentTrackAttributes) || closeElement;
                if (closePacket) {
                    closePacket();
                }
                if (closeElement) {
                    closeElement();
                }
                trackAttributes = currentTrackAttributes;
                if (in.getContext().getSmpte436mOutputStreamFilter().contains(header.getId())) {
                    if (in.getContext().isDebug()) {
                        in.getContext().debug("filtered " + header.getId() + "; track attr: " + trackAttributes);
                    }
                    continue;
                }
                sampleOutputStream.write(header.getId().getDid());
                sampleOutputStream.write(header.getId().getSdid());
                sampleOutputStream.write(header.getDataCount());
                for (int word : data.getDataWords()) {
                    sampleOutputStream.write(word);
                }
                sampleCount += (3 + header.getDataCount());
            } while (true);
        } catch (EOFException eof) {
            log.info("END of SMPTE 291 stream");
        } catch (IOException e) {
            log.error("SMPTE 291 IOException", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                closePacket();
                closeElement();
                smpte436MOutputStream.flush();
                smpte436MOutputStream.close();
            } catch (IOException ioe) {
                log.error("failed finishing S436 output stream", ioe);
            }
        }
    }

    private void closeElement() throws IOException
    {
        smpte436MOutputStream.writeShort(packetCount);
        smpte436MOutputStream.write(packetBuffer.toByteArray());
        packetBuffer.reset();
        packetCount = 0;
    }

    private void closePacket() throws IOException
    {
        packetOutputStream.writeShort(trackAttributes.getLine());
        packetOutputStream.write(0x1); // wrapping-type, forced to VANC Frame
        packetOutputStream.write(0x4); // payload sample coding
        packetOutputStream.writeShort(sampleCount); // payload sample count
        // write array element count, which must be multiple of 4
        int pad = sampleCount % 4;
        int extra = 4 - pad;
        if (pad != 0) {
            packetOutputStream.writeInt(sampleCount + extra);
        } else {
            packetOutputStream.writeInt(sampleCount);
        }
        packetOutputStream.writeInt(1); // array element size is 1 byte
        packetOutputStream.write(sampleOutputStream.toByteArray());
        if (pad != 0) {
            for (int i = 0; i < extra; i++) {
                packetOutputStream.write(0);
            }
        }
        sampleOutputStream.reset();
        sampleCount = 0;
        packetCount++;
    }
}
