package org.foraci.anc.anc;

import org.foraci.anc.util.timecode.Timecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Writes a file containing field CC bytes in the correct order (i.e. field1, field2, ...)
 *
 * @author Joe Foraci
 */
public class CcdWriter implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(CcdWriter.class);

    private InputStream incc1;
    private InputStream incc3;
    private OutputStream out1, out2;
    private AncTrackReader context;
    private boolean top;
    private Timecode timecode;

    public CcdWriter(InputStream incc1, InputStream incc3, OutputStream out, AncTrackReader context)
    {
        this.incc1 = incc1;
        this.incc3 = incc3;
        try {
            this.out1 = new FileOutputStream("cc1.scc");
            this.out2 = new FileOutputStream("cc2.scc");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.context = context;
        final Timecode offset = context.getOffset();
        this.timecode = Timecode.fromEditUnits(offset.getTimecodeBase(), offset.getPosition());
    }

    public void run()
    {
        top = true;
        try {
            writeHeader();
            mix();
        } catch (EOFException eof) {
            // catch and finish up other input stream
        } catch (IOException e) {
            log.error("failed muxing", e);
            return;
        }
        try {
            if (top) {
                finish(incc3);
            } else {
                finish(incc1);
            }
        } catch (EOFException eof2) {
            // catch and write last null bottom field if necessary
        } catch (IOException e) {
            log.error("failed finishing", e);
            return;
        }
        try {
            writeBottomField();
        } catch (IOException e) {
            log.error("failed writing bottom", e);
            return;
        }
        try {
            close();
        } catch (IOException ioe) {
            log.error("failed to close", ioe);
        }
        log.info("CCD Writer done");
    }

    private void close() throws IOException {
        out1.flush();
        out1.close();
        out2.flush();
        out2.close();
    }

    private void writeHeader() throws IOException {
        out1.write("Scenarist_SCC V1.0\r\n".getBytes("ISO-8859-1"));
        out2.write("Scenarist_SCC V1.0\r\n".getBytes("ISO-8859-1"));
    }

    private void mix() throws IOException
    {
        int data1, data2;
        String tc, line;
        do {
            // CC1
            data1 = readWord(incc1);
            data2 = readWord(incc1);
            tc = timecode.getLabel().toString();
            line = tc + " " + pad(data1 & 0x7f) + pad(data2 & 0x7f) + "\r\n";
            context.debug(line);
            out1.write(line.getBytes("ISO-8859-1"));
            top = false;
            // CC3
            data1 = readWord(incc3);
            data2 = readWord(incc3);
            line = tc + " " + pad(data1 & 0x7f) + pad(data2 & 0x7f) + "\r\n";
            context.debug(line);
            out2.write(line.getBytes("ISO-8859-1"));
            timecode.increment();
            top = true;
        } while (true);
    }

    private void finish(InputStream in) throws IOException
    {
        int data1, data2;
        String tc, line;
        boolean zero = true;
        do {
            if (zero) {
                data1 = 0;
                data2 = 0;
            } else {
                data1 = readWord(in);
                data2 = readWord(in);
            }
            top = !top;
            zero = !zero;
            tc = timecode.getLabel().toString();
            line = tc + " " + pad(data1 & 0x7f) + pad(data2 & 0x7f) + "\r\n";
            context.debug(line);
            if (!top) {
                out1.write(line.getBytes("ISO-8859-1"));
            } else {
                out2.write(line.getBytes("ISO-8859-1"));
            }
            if (top) {
                timecode.increment();
            }
        } while (true);
    }

    private void writeBottomField() throws IOException
    {
        if (top) {
            return;
        }
        int data1, data2;
        String tc, line;
        data1 = 0;
        data2 = 0;
        top = true;
        tc = timecode.getLabel().toString();
        line = tc + " " + pad(data1 & 0x7f) + pad(data2 & 0x7f) + "\r\n";
        context.debug(line);
        out2.write(line.getBytes("ISO-8859-1"));
        timecode.increment();
    }

    private int readWord(InputStream in) throws IOException
    {
        int i = in.read();
        if (i == -1) {
            throw new EOFException();
        }
        return i;
    }

    private static String pad(int i)
    {
        if (i < 16) return "0" + Integer.toHexString(i);
        return Integer.toHexString(i);
    }
}
