package org.foraci.anc.util;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author Joe Foraci
 */
public class Utils
{
    public static void skipFully(InputStream in, long len) throws IOException
    {
        do {
            len -= in.skip(len);
        } while (len > 0);
    }
}
