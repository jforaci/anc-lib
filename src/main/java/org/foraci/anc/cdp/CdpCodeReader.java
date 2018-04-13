package org.foraci.anc.cdp;

import org.foraci.anc.anc.AncTrackReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.IOException;

/**
 * Reads the caption data packet code layer
 *
 * @author Joe Foraci
 */
public class CdpCodeReader implements Runnable
{
    private static final Logger log = LoggerFactory.getLogger(CdpCodeReader.class);

    private int serviceNumber;
    private InputStream in;
    private AncTrackReader context;
    private StringBuffer captionBuffer;
    private boolean endCaption, textSent;

    public CdpCodeReader(int serviceNumber, InputStream in, AncTrackReader context)
    {
        this.serviceNumber = serviceNumber;
        this.in = in;
        this.context = context;
        this.captionBuffer = null;
        this.endCaption = false;
        this.textSent = false;
    }

    private static final int EXT1 = 0x10;
    private static final int P16 = 0x18;

    public void run()
    {
        captionBuffer = new StringBuffer();
        int b;
        try {
            while ((b = in.read()) != -1) {
//                context.debug(Integer.toHexString(b));
                if (b == EXT1) {
                    processExtCharacter();
                } else if (b == P16) {
                    // the P16 extension code isn't used in the CEA-708 specification at this time
                    context.debug("P16: " + Integer.toHexString(in.read()) + Integer.toHexString(in.read()));
                } else if (b >= 0x00 && b <= 0x1F) { // C0 set in the CL group
                    processControlGroup(b);
                } else if (b >= 0x80 && b <= 0x9F) { // C1 set in the CR group
                    processCaptioningCommandCodes(b);
                } else if (b >= 0x20 && b <= 0x7F) { // G0 set in the GL group
                    processAsciiCharacter(b);
                } else if (b >= 0xA0 && b <= 0xFF) { // G1 set in the GR group
                    processIso88591Character(b);
                }
                if (endCaption) {
                    context.debug("LINE: " + captionBuffer.toString());
                    if (serviceNumber == 1) System.out.println("LINE: " + captionBuffer.toString());
                    if (context.getCaptionSink708() != null) {
                        char[] buff = new char[captionBuffer.length()];
                        captionBuffer.getChars(0, captionBuffer.length(), buff, 0);
                        context.getCaptionSink708().write(buff, 0, buff.length, serviceNumber);
                    }
                    captionBuffer.setLength(0);
                    endCaption = false;
                    textSent = false;
                }
            }
            log.info("END of CDP CODE stream");
        } catch (IOException e) {
            log.error("error reading CDP stream", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private void endCaption()
    {
        if (!textSent) {
            return;
        }
        endCaption = true;
    }

    private void charactersSent(char c)
    {
        captionBuffer.append(c);
        textSent = true;
    }

    private void processControlGroup(int b) throws IOException
    {
        if (b == 0x0C || b == 0x0D || b == 0x0E) {
            // FF, CR, HCR constitute a "caption segment" for display purposes
            endCaption();
        } else if (b == 0x03 || b == 0x00 || b == 0x08) {
            // NUL, ETX, BS are ignored
        } else {
            // note: the EXT1 and P16 codes are handled before this method is invoked
            if (b >= 0x10 && b <= 0x17) {
                in.skip(1); // skip the following reserved byte (in the 2-byte sequence)
                context.debug("unknown C0 2-byte sequence start code: " + Integer.toHexString(b));
            } else if (b >= 0x18 && b <= 0x1F) {
                in.skip(2); // skip the following 2 reserved bytes (in the 3-byte sequence)
                context.debug("unknown C0 3-byte sequence start code: " + Integer.toHexString(b));
            } else {
                context.debug("unknown C0 byte code: " + Integer.toHexString(b));
            }
        }
    }

    private void processExtCharacter() throws IOException
    {
        int b = in.read();
        if (b >= 0x00 && b <= 0x1F) {
            processC2Code(b);
        } else if (b >= 0x20 && b <= 0x7F) {
            b = b - 0x20 + 0x80;
            processG2Character(b);
        } else if (b >= 0x80 && b <= 0x9F) {
            processC3Code(b);
        } else if (b >= 0xA0 && b <= 0xFF) {
            b = 0x20;
            processG3Character(b);
        }
    }

    private void processC2Code(int b) throws IOException
    {
        // TODO: skipping unknown reserved control codes (as of CEA 708-D)
        if (b >= 0x08) in.read();
        if (b >= 0x10) in.read();
        if (b >= 0x18) in.read();
    }

    private void processC3Code(int b) throws IOException
    {
        // TODO: skipping unknown reserved control codes (as of CEA 708-D)
        if (b >= 0x80 && b <= 0x8F) {
            in.skip(4);
            if (b >= 0x88) in.read();
        } else if (b >= 0x90 && b <= 0x9F) {
            int len = (in.read() & 0x1F);
            for (int i = 0; i < len; i++) {
                in.read();
            }
        }
    }

    private void processG2Character(int b)
    { // TODO: display underline (0x5F) or space character (0x20) for undefined codes
        char c = (char) b;
        charactersSent(c);
    }

    private void processG3Character(int b)
    { // TODO: display underline (0x5F) or space character (0x20) for undefined codes
        char c = (char) b;
        charactersSent(c);
    }

    private void processIso88591Character(int b)
    {
        char c = (char) b;
        charactersSent(c);
    }

    private void processAsciiCharacter(int b)
    {
        if (b == 0x7F) {
            charactersSent('\u266a'); // music note
        } else {
            char c = (char) b;
            charactersSent(c);
        }
    }

    private void processCaptioningCommandCodes(int b) throws IOException
    {
        if (b >= 0x80 && b <= 0x87) { // set current window
            endCaption();
            captionBuffer.append("{CW" + (b & 0x3) + "}");
        }
        if (b >= 0x98 && b <= 0x9F) { // define window
            for (int i = 0; i < 6; i++) {
                in.read();
            }
            endCaption();
            captionBuffer.append("{DF" + ((b & 0xF) - 0x8) + "}");
        }
        if (b == 0x88) { // clear windows
            in.read();
            endCaption();
            captionBuffer.append("{CLW}");
        }
        if (b == 0x89) { // display windows
            in.read();
            endCaption();
            captionBuffer.append("{DSW}");
        }
        if (b == 0x8A) { // hide windows
            in.read();
            endCaption();
            captionBuffer.append("{HDW}");
        }
        if (b == 0x8B) { // toggle windows
            in.read();
            endCaption();
            captionBuffer.append("{TGW}");
        }
        if (b == 0x8C) { // delete windows
            in.read();
            endCaption();
            captionBuffer.append("{DLW}");
        }
        if (b == 0x8D) { // delay
            in.read(); // tenths of a second
            endCaption();
            captionBuffer.append("{DLY}");
        }
        if (b == 0x8E) { // delay cancel
            endCaption();
            captionBuffer.append("{DLC}");
        }
        if (b == 0x8F) { // reset
            endCaption();
            captionBuffer.append("{RST}");
        }
        if (b == 0x90) { // set pen attributes
            in.read();
            in.read();
            endCaption();
            captionBuffer.append("{SPA}");
        }
        if (b == 0x91) { // set pen color
            for (int i = 0; i < 3; i++) {
                in.read();
            }
            endCaption();
            captionBuffer.append("{SPC}");
        }
        if (b == 0x92) { // set pen location
            for (int i = 0; i < 2; i++) {
                in.read();
            }
            endCaption();
            captionBuffer.append("{SPL}");
        }
        if (b == 0x97) { // set window attributes
            for (int i = 0; i < 4; i++) {
                in.read();
            }
            endCaption();
            captionBuffer.append("{SWA}");
        }
    }
}
