package org.foraci.anc.cdp;

import org.foraci.anc.anc.AncTrackReader;
import org.foraci.anc.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

/**
 * Reads the caption channel packet and service layers
 *
 * @author Joe Foraci
 */
public class CdpReader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(CdpReader.class);

    private InputStream in;
    private AncTrackReader context;

    public CdpReader(InputStream in, AncTrackReader context)
    {
        this.in = in;
        this.context = context;
    }

    public static final int STATE_START = 0;
    public static final int STATE_IN_PACKET = 1;
    public static final int STATE_IN_SERVICE_BLOCK_HEADER = 2;
    public static final int STATE_IN_SERVICE_BLOCK_PYLD = 3;
    private int sequence = -1;
    private int count = 0;
    private int offset = 0;
    private Map codeReaders = new HashMap();
    private byte[][] packetBuffer = new byte[64][];
    private int[] packetBufferOffset = new int[64];

    private int read() throws IOException
    {
        offset++;
        int b = in.read();
//        log(Integer.toHexString(b));
        return b;
    }

    public void run()
    {
        int b;
        int serviceNumber = 0;
        int serviceBlockSize = 0;
        int lastSequence = -1;
        try {
            int state = STATE_START;
            do {
                if (offset > count) {
                    throw new IllegalStateException("over-read");
                }
                if (state != STATE_START && offset == count) {
                    state = STATE_START;
                }
                if ((b = read()) == -1) {
                    break;
                }
                if (state == STATE_START) {
                    sequence = ((b >> 6) & 0x3);
                    if (sequence != (lastSequence + 1) % 4) {
                        log.warn("discontinuous sequence number: " + lastSequence + " to " + sequence);
                        // FIX: according to CEA-708-D, sec 5, if a discontinuity of sequence numbers occurs,
                        // then the previous packet's data should be discarded but this doesn't seem to work
                        // with asset tested (780469) -- might mean data at lower level ("sytactic elements"...)
//                        discardPacketBuffers();
                        flushPacketBuffers();
                    } else {
                        flushPacketBuffers();
                    }
                    lastSequence = sequence;
//                    log("sequence="+sequence);
                    count = (b & 0x3F);
                    offset = 0;
                    if (count == 0) {
                        count = 128 - 1; // minus this 1-byte header
                    } else {
                        count = count * 2 - 1; // minus this 1-byte header
                    }
                    state = STATE_IN_PACKET;
                    continue;
                }
                if (state == STATE_IN_PACKET) {
                    state = STATE_IN_SERVICE_BLOCK_HEADER;
                } // fall thru...
                if (state == STATE_IN_SERVICE_BLOCK_HEADER) {
                    serviceNumber = ((b >> 5) & 0x7);
                    serviceBlockSize = (b & 0x1F);
                    if (offset + serviceBlockSize > count) {
                        log.warn("service block size of " + serviceBlockSize + " overruns CDP length of " + count);
                        Utils.skipFully(in, count - offset);
                        state = STATE_START;
                        continue;
                    }
                    if (serviceNumber == 0) {
                        if (serviceBlockSize != 0) {
                            log.warn("service 0 has non-zero service block size: " + serviceBlockSize);
                            Utils.skipFully(in, count - offset);
                            state = STATE_START;
                        } else {
                            state = STATE_IN_PACKET;
                        }
                        continue;
                    }
                    if (serviceNumber == 0x7) {
                        if ((b = read()) == -1) {
                            throw new IOException("end of stream");
                        }
                        serviceNumber = (b & 0x3F); // extended service number
                        if (serviceNumber < 7) {
                            log.warn("service number is less than 7 (" + serviceNumber
                                    + ") was received in an Extended Service Block header");
                        }
                    }
                    // write out service data to consumers
//                    PipedOutputStream out = (PipedOutputStream) codeReaders.get(new Integer(serviceNumber));
//                    if (out == null) {
//                        out = new PipedOutputStream();
//                        CdpCodeReader cdpCodeReader = new CdpCodeReader(serviceNumber, new PipedInputStream(out), context);
//                        new Thread(cdpCodeReader, "CODE-READER-" + serviceNumber).start();
//                        codeReaders.put(new Integer(serviceNumber), out);
//                    }
//                    for (int i = 0; i < serviceBlockSize; i++) {
//                        if ((b = read()) == -1) {
//                            throw new IOException("end of stream");
//                        }
//                        out.write(b);
//                    }
                    state = STATE_IN_PACKET;
                    pushToPacketBuffer(serviceNumber, serviceBlockSize);
                    continue;
                }
            } while (true);
            log.info("END of CDP stream");
        } catch (IOException e) {
            log.error("error reading CDP stream", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            try {
                flushPacketBuffers();
            } catch (IOException e) {
                log.error("error flushing packet buffers", e);
            }
            for (Iterator i = codeReaders.keySet().iterator(); i.hasNext();) {
                serviceNumber = ((Integer) i.next()).intValue();
                PipedOutputStream out = (PipedOutputStream) codeReaders.get(new Integer(serviceNumber));
                try {
                    out.flush();
                    out.close();
                    log.trace("closed stream for service " + serviceNumber);
                } catch (IOException e) {
                    log.error("error flushing CDP readers", e);
                }
            }
        }
    }

    private void discardPacketBuffers() throws IOException
    {
        for (int serviceNumber = 0; serviceNumber < packetBufferOffset.length; serviceNumber++) {
            packetBufferOffset[serviceNumber] = 0;
        }
    }

    private void flushPacketBuffers() throws IOException
    {
        for (int serviceNumber = 0; serviceNumber < packetBufferOffset.length; serviceNumber++) {
            if (packetBufferOffset[serviceNumber] == 0) {
                continue;
            }
            PipedOutputStream out = (PipedOutputStream) codeReaders.get(new Integer(serviceNumber));
            if (out == null) {
                out = new PipedOutputStream();
                CdpCodeReader cdpCodeReader = new CdpCodeReader(serviceNumber, new PipedInputStream(out), context);
                new Thread(cdpCodeReader, "CODE-READER-" + serviceNumber).start();
                codeReaders.put(new Integer(serviceNumber), out);
            }
            for (int i = 0; i < packetBufferOffset[serviceNumber]; i++) {
                int b = packetBuffer[serviceNumber][i] & 0xFF;
                out.write(b);
            }
            packetBufferOffset[serviceNumber] = 0;
        }
    }

    private void pushToPacketBuffer(int serviceNumber, int serviceBlockSize) throws IOException
    {
        int b;
        if (packetBuffer[serviceNumber] == null) {
            packetBuffer[serviceNumber] = new byte[128];
        }
        for (int i = 0; i < serviceBlockSize; i++) {
            if ((b = read()) == -1) {
                throw new IOException("end of stream");
            }
            packetBuffer[serviceNumber][packetBufferOffset[serviceNumber]++] = (byte)(b & 0xFF);
        }
    }
}
