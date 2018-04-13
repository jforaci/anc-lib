package org.foraci.anc.anc;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A collection of {@link TrackAttributes}. This implementation is thread-safe.
 *
 * @author Joe Foraci
 */
class TrackAttributesCollection
{
    private final LinkedList<TrackAttributes> trackAttributesList;

    TrackAttributesCollection() {
        this.trackAttributesList = new LinkedList<TrackAttributes>();
    }

    public void addTrackAttributes(TrackAttributes trackAttributes)
    {
        if (trackAttributes != null) {
            synchronized (trackAttributesList) {
                trackAttributesList.add(trackAttributes);
            }
        }
    }

    public TrackAttributes getTrackAttributes(long offset)
    {
        TrackAttributes trackAttributes = null;
        int trimTo = -1;
        synchronized (trackAttributesList) {
            // find track attribute entry for the given offset, if any
            for (Iterator<TrackAttributes> i = trackAttributesList.iterator(); i.hasNext();) {
                TrackAttributes attr = i.next();
                if (attr.getOffset() > offset) {
                    break;
                }
                trackAttributes = attr;
                trimTo++;
            }
            // trim unused entries
            for (int i = 0; i < trimTo; i++) {
                trackAttributesList.removeFirst();
            }
        }
        return trackAttributes;
    }
}
