package org.aries.middleware.hermes.common.utils;

public class LongUtil {

    public static long combine(int low, int high) {
        return ((long) low & 0xFFFFFFFFL | (((long) high << 32) & 0xFFFFFFFF00000000L));

    }

    public static int[] separate(long val) {
        int[] ret = new int[2];

        ret[0] = (int) (0xFFFFFFFFL & val);
        ret[1] = (int) ((0xFFFFFFFF00000000L & val) >> 32);

        return ret;
    }
}
