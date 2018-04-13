package org.foraci.anc.anc;

import org.foraci.anc.cc.CaptionsSink;
import org.foraci.anc.cdp.CdpReader;
import org.foraci.anc.util.io.CountingInputStream;
import org.foraci.anc.util.timecode.Timecode;
import org.foraci.anc.util.timecode.TimecodeBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Ancillary data track reader. <code>doRead()</code> reads the transport for the SMPTE 291
 * data (e.g. RIFF for GXF, 436m for MXF, etc.) and <code>createSmpte291InputStream()</code>
 * is called to create a <code>Smpte291InputStream</code> implementation to read the anc words
 * to construct <code>AncPacketHeader</code> and <code>AncPacketUserData</code> packets
 *
 * @author Joe Foraci
 */
public abstract class AncTrackReader
{
    private static final Logger log = LoggerFactory.getLogger(AncTrackReader.class);

    protected DataInputStream in = null;
    private CountingInputStream cin = null;
    private PipedOutputStream ancOutputStream;
    private PipedInputStream ancInputStream;
    private long smpte291StreamOffset;
    private Timecode offset;
    private boolean debug = false;
    private Map attr = new HashMap();
    private List<AncPacketListener> ancPacketListeners;
    private List<AncPacketId> smpte436OutputPacketFilterList;

    public AncTrackReader(InputStream in) throws IOException
    {
        cin = new CountingInputStream(new BufferedInputStream(in));
        this.in = new DataInputStream(cin);
        this.smpte291StreamOffset = 0;
        this.ancPacketListeners = new LinkedList<AncPacketListener>();
        this.smpte436OutputPacketFilterList = Collections.emptyList();
        this.offset = Timecode.fromEditUnits(TimecodeBase.NTSC, 0);
    }

    public void read() throws IOException, InterruptedException
    {
        ancOutputStream = new PipedOutputStream();
        ancInputStream = new PipedInputStream(ancOutputStream);
        Smpte291InputStream smpte291InputStream = createSmpte291InputStream(ancInputStream);
        smpte291Reader = new Smpte291Reader(smpte291InputStream);
        Thread smpte291ReaderThread = new Thread(smpte291Reader, "SMPTE291-READER");
        smpte291ReaderThread.start();
        Thread smpte436WriterThread = null;
        if (getSmpte436mOutputSteam() != null) {
            ancS291ToS436OutputStream = new PipedOutputStream();
            ancS291ToS436InputStream = new PipedInputStream(ancS291ToS436OutputStream);
            Smpte291InputStream auxSmpte291InputStream = createSmpte291InputStream(ancS291ToS436InputStream);
            smpte436Writer = new Smpte436Writer(auxSmpte291InputStream, getSmpte436mOutputSteam());
            smpte436WriterThread = new Thread(smpte436Writer, "SMPTE291-WRITER");
            smpte436WriterThread.start();
        }
        // 708 CC threads
        Thread cdpReaderThread = null;
        if (isDecode708()) {
            cdpOutputStream = new PipedOutputStream();
            cdpInputStream = new PipedInputStream((PipedOutputStream)cdpOutputStream);
            cdpReaderThread = new Thread(new CdpReader(cdpInputStream, this), "CDP-READER");
            cdpReaderThread.start();
        }
        // 608 CC threads
        Thread ccdWriterThread = null;
        if (getCcd608OutputStream() != null) {
            final int serviceBufferSize = 4 * 1024 * 1024;
            cc1OutputStream = new PipedOutputStream();
            cc1InputStream = new CustomPipedInputStream(cc1OutputStream, serviceBufferSize);
            cc3OutputStream = new PipedOutputStream();
            cc3InputStream = new CustomPipedInputStream(cc3OutputStream, serviceBufferSize);
            ccdWriterThread = new Thread(new CcdWriter(cc1InputStream, cc3InputStream, getCcd608OutputStream(), this), "CCD-WRITER");
            ccdWriterThread.start();
        }
        // start reading
        try {
            doRead(ancOutputStream);
        } catch (EOFException eof) {
            if (isDebug()) {
                log.error("EOF reached", eof);
            }
        } finally {
            ancOutputStream.flush();
            ancOutputStream.close();
            smpte291ReaderThread.join();
            if (isDecode708()) {
                cdpReaderThread.join();
            }
            if (getSmpte436mOutputSteam() != null) {
                ancS291ToS436OutputStream.flush();
                ancS291ToS436OutputStream.close();
                smpte436WriterThread.join();
            }
            if (getCcd608OutputStream() != null) {
                ccdWriterThread.join();
            }
        }
        log.info("AncTrackReader done");
    }

    protected abstract Smpte291InputStream createSmpte291InputStream(InputStream inputStream);

    protected abstract void doRead(OutputStream ancillaryOutputStream) throws IOException;

    protected final void write(byte[] data, int offset, int length, TrackAttributes trackAttributes) throws IOException
    {
        if (trackAttributes != null) {
            trackAttributes.setOffset(smpte291StreamOffset);
        }
        smpte291Reader.addTrackAttributes(trackAttributes);
        ancOutputStream.write(data, offset, length);
        if (getSmpte436mOutputSteam() != null) {
            smpte436Writer.addTrackAttributes(trackAttributes);
            ancS291ToS436OutputStream.write(data, offset, length);
        }
        smpte291StreamOffset += length;
    }

    private boolean decode708 = false;
    private Smpte291Reader smpte291Reader;
    private OutputStream cdpOutputStream;
    private InputStream cdpInputStream;
    private CaptionsSink captionSink708;
    private OutputStream ccd608OutputStream;
    private PipedOutputStream cc1OutputStream, cc3OutputStream;
    private PipedInputStream cc1InputStream, cc3InputStream;
    private DataOutputStream smpte436mOutputSteam;
    private Smpte436Writer smpte436Writer;
    private PipedOutputStream ancS291ToS436OutputStream;
    private PipedInputStream ancS291ToS436InputStream;

    public boolean isDecode708()
    {
        return decode708;
    }

    public void setDecode708(boolean decode708)
    {
        this.decode708 = decode708;
    }

    public void register708CaptionSink(CaptionsSink sink)
    {
        this.captionSink708 = sink;
    }

    public CaptionsSink getCaptionSink708()
    {
        return captionSink708;
    }

    public OutputStream getCdpOutputStream()
    {
        return cdpOutputStream;
    }

    public OutputStream getCC1OutputStream()
    {
        return cc1OutputStream;
    }

    public OutputStream getCC3OutputStream()
    {
        return cc3OutputStream;
    }

    public OutputStream getCcd608OutputStream()
    {
        return ccd608OutputStream;
    }

    public void setCcd608OutputStream(OutputStream outputStream)
    {
        ccd608OutputStream = outputStream;
    }

    public DataOutputStream getSmpte436mOutputSteam()
    {
        return smpte436mOutputSteam;
    }

    public void setSmpte436mOutputStream(OutputStream smpte436mOutputSteam)
    {
        if (smpte436mOutputSteam != null) {
            this.smpte436mOutputSteam = new DataOutputStream(smpte436mOutputSteam);
        } else {
            this.smpte436mOutputSteam = null;
        }
    }

    public void setSmpte436mOutputStreamFilter(List<AncPacketId> packetFilterList)
    {
        if (packetFilterList == null) {
            throw new NullPointerException("packetFilterList may not be null");
        }
        this.smpte436OutputPacketFilterList = packetFilterList;
    }

    public List<AncPacketId> getSmpte436mOutputStreamFilter()
    {
        return smpte436OutputPacketFilterList;
    }

    protected void setOffset(Timecode offset) {
        this.offset = offset;
    }

    public Timecode getOffset() {
        return offset;
    }

    public long getPosition()
    {
        return cin.getPosition();
    }

    public boolean isDebug()
    {
        return debug;
    }

    public void setDebug(boolean debug)
    {
        this.debug = debug;
    }

    public void debug(String message)
    {
        if (!debug) {
            return;
        }
        log.debug(message);
    }

    public void setAttribute(String key, Object object)
    {
        attr.put(key, object);
    }

    public Object getAttribute(String key)
    {
        return attr.get(key);
    }

    void fireAncPacketEvent(AncPacketHeader header, AncPacketUserData payload, TrackAttributes trackAttributes)
    {
        for (AncPacketListener listener : ancPacketListeners) {
            listener.packet(header, payload, trackAttributes);
        }
    }

    public void addAncPacketListener(AncPacketListener listener)
    {
        ancPacketListeners.add(listener);
    }

    public void removeAncPacketListener(AncPacketListener listener)
    {
        ancPacketListeners.remove(listener);
    }
}
