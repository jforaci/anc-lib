package org.foraci.anc.util.timecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NtscConverter {
    private static final Logger log = LoggerFactory.getLogger(NtscConverter.class);

    private static final int NTSC_FIELDS_PER_DAY = 5178816;
    private static final int NTSC_FRAMES_PER_HOUR = 107892;
    private static final int NTSC_FRAMES_PER_TEN_MINUTES = 17982;
    private static final int NTSC_FRAMES_PER_MINUTE = 1800;
    private static final int NTSC_FRAMES_PER_MINUTE_WITH_DROP = 1798;
    private static final int NTSC_FRAMES_PER_SEC = 30;
    private static final int[] NTSC_TABLE_STARTING_FRAME_PER_MINUTE = {0, 1800, 3598, 5396, 7194, 8992, 10790, 12588, 14386, 16184};

    /**
     * This method takes a time string formatted as hh:mm:ss.ff and a boolean
     * duration conversion flag and returns an int fields counter.
     * A false for the duration will use the Louth/Harris time code formula.
     * A true for the duration will use the Ampex/ACR225 calculation.
     * Normal calls should always use the false for duration.
     *
     * @return int fields (not frames).
     **/
    public int convertToFrames(String timecode, boolean duration) {
        int hh = Integer.parseInt(timecode.substring(0, 2));
        int mm = Integer.parseInt(timecode.substring(3, 5));
        int ss = Integer.parseInt(timecode.substring(6, 8));
        int ff = Integer.parseInt(timecode.substring(9));
        return convertToFrames(hh, mm, ss, ff, duration);
    }

    /**
     * This method takes a int time values for hh:mm:ss.ff and a boolean
     * duration conversion flag and returns an int fields counter.
     * A false for the duration will use the Louth/Harris time code formula.
     * A true for the duration will use the Ampex/ACR225 calculation.
     * Normal calls should always use the false for duration.
     *
     * @return int fields (not frames).
     **/
    public int convertToFrames(int hh, int mm, int ss, int ff, boolean duration) {
        int tim;
        int j, k;
        int fr;
        fr = ff;
        if (hh >= 24 || mm >= 60 || ss >= 60 || ff >= NTSC_FRAMES_PER_SEC) return 0;
        tim = hh * NTSC_FRAMES_PER_HOUR;
        tim = tim + ((mm / 10) * NTSC_FRAMES_PER_TEN_MINUTES);
        /* if duration conversion modify the time */
        if (duration) {
            k = NTSC_FRAMES_PER_MINUTE * (mm % 10) + ss * NTSC_FRAMES_PER_SEC + fr;
            j = (k + 499) / 999;
            k = k - j;
            if (j != ((k + 499) / 999)) ++k;
        } else {
            int y = mm % 10;
            // if (y != 0 && ss == 0 && ff < 2) fr = 2; /* you never have minutes and no frames */
            k = NTSC_TABLE_STARTING_FRAME_PER_MINUTE[y] + (ss * NTSC_FRAMES_PER_SEC) + fr;
            if (y != 0) k = k - 2;
        }
        tim = tim + k;
        tim <<= 1; /* always even field */
        return tim;
    }

    /**
     * This method takes a int fields time (not frames) and a boolean
     * duration conversion flag and returns a string format hh:mm:ss.ff.
     * A false for the duration will use the Louth/Harris time code formula.
     * A true for the duration will use the Ampex/ACR225 calculation.
     * Normal calls should always use the false for duration.
     *
     * @return String time.
     **/
    public String convertFromFrames(int fields, boolean duration) {
        int x;
        int y;
        int tim;
        tim = fields;
        if (tim < 0) {
            log.error("Negative Frame Time");
            return null;
        }
        if (tim >= NTSC_FIELDS_PER_DAY) tim = tim - NTSC_FIELDS_PER_DAY;
        tim >>= 1;
        int hh = tim / NTSC_FRAMES_PER_HOUR;    /* hours */
        tim = tim % NTSC_FRAMES_PER_HOUR;
        int mm = tim / NTSC_FRAMES_PER_TEN_MINUTES;                /* 10 minutes */
        mm = mm * 10;
        x = tim % NTSC_FRAMES_PER_TEN_MINUTES;
        if (duration) {
            y = ((x >= 14384) ? (x - 2) : (x >= 5394) ? (x - 1) : x) / NTSC_FRAMES_PER_MINUTE_WITH_DROP;
            x += (x + 499) / 999 - y * NTSC_FRAMES_PER_MINUTE;
        } else {
            y = x / NTSC_FRAMES_PER_MINUTE;
            x = x - NTSC_TABLE_STARTING_FRAME_PER_MINUTE[y];
            if (y > 0 && x >= NTSC_FRAMES_PER_MINUTE_WITH_DROP) {
                ++y;
                x = x - NTSC_FRAMES_PER_MINUTE_WITH_DROP;
            }
            if (y != 0) x = x + 2;
        }
        mm = mm + y;
        int ss = x / NTSC_FRAMES_PER_SEC;
        int ff = x % NTSC_FRAMES_PER_SEC;
        return String.format("%02d:%02d:%02d.%02d", hh, mm, ss, ff);
    }

    private String pad(int number) {
        if (number > 9) return String.valueOf(number);
        String padded = "0" + number;
        return padded;
    }
}