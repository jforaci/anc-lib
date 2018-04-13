package org.foraci.anc.anc;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;

/**
 * A <code>PipedInputStream</code> with a customizable circular buffer size
 *
 * @author Joe Foraci
 */
public class CustomPipedInputStream extends PipedInputStream
{
    public CustomPipedInputStream(int bufferSize)
    {
        this.buffer = new byte[bufferSize];
    }

    public CustomPipedInputStream(PipedOutputStream out, int bufferSize) throws IOException
    {
        super(out);
        this.buffer = new byte[bufferSize];
    }
}
