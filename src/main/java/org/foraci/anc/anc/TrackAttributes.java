package org.foraci.anc.anc;

/**
 * Attributes of where the SMPTE 291 packets were read, e.g. field line number, whether they were
 * in HANC/VANC, from a chroma/luma sample, etc.
 * This is basically modelled after the information found in GXF's VANC track.
 */
public class TrackAttributes
{
    private long offset;
    private final int field;
    private final int line;
    private final boolean inHanc;
    private final boolean chroma;

    public TrackAttributes(int field, int line, boolean inHanc, boolean chroma)
    {
        this.field = field;
        this.line = line;
        this.inHanc = inHanc;
        this.chroma = chroma;
    }

    long getOffset()
    {
        return offset;
    }

    void setOffset(long offset)
    {
        this.offset = offset;
    }

    public boolean isChroma()
    {
        return chroma;
    }

    public int getField()
    {
        return field;
    }

    public boolean isInHanc()
    {
        return inHanc;
    }

    public int getLine()
    {
        return line;
    }

    @Override
    public String toString()
    {
        return String.format("line=%d,field=%d,chroma=%b,hanc=%b,offset=0x%x",
                line, field, chroma, inHanc, offset);
    }
}
