package org.foraci.anc.util.io;

import java.io.*;

/**
 * An input stream that interleaves two <code>InputStream</code>s
 * @author Joe Foraci
 */
public class MultiplexingInputStream extends DataInputStream
{
    private DataInputStream first;
    private DataInputStream second;
    private int limit;
    private int count = 0;
    private DataInputStream markIn = null;
    private int markCount = 0;

    public MultiplexingInputStream(DataInputStream first, DataInputStream second, int limit)
    {
        super(first);
        this.first = first;
        this.second = second;
        this.limit = limit;
        this.in = first;
    }

    private void toggle()
    {
        if (in == first && second != null) {
            in = second;
        } else {
            in = first;
        }
        count = 0;
    }

    public void align() throws IOException
    {
        align(first);
        if (second != null) {
            align(second);
        }
    }

    private void align(InputStream inputStream) throws IOException
    {
        final int lim = 1024;
        do {
            inputStream.mark(lim);
            int b = inputStream.read();
            if (b == -1) {
                throw new EOFException("error aligning!");
            }
            if (b != 0) {
                inputStream.reset();
                break;
            }
        } while (true);
    }

    public long skip(long n) throws IOException
    {
        long s;
        s = first.skip(n);
        if (second != null) {
            s = second.skip(n);
        }
        return s;
    }

    public synchronized void mark(int readlimit)
    {
        markIn = (DataInputStream) in;
        markCount = count;
        first.mark(readlimit);
        if (second != null) {
            second.mark(readlimit);
        }
    }

    public synchronized void reset() throws IOException
    {
        in = markIn;
        count = markCount;
        first.reset();
        if (second != null) {
            second.reset();
        }
    }

    public int read() throws IOException
    {
        if (count >= limit) toggle();
        int b = super.read();
        count++;
        return b;
    }
}
