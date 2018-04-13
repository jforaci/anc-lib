package org.foraci.anc.cc;

import java.io.IOException;

/**
 * A simple interface to send decoded captions to
 */
public interface CaptionsSink {
    void write(char cbuf[], int off, int len, int serviceNumber) throws IOException;
}
