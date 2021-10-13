package org.aries.middleware.hermes.telemetry;


import cn.hutool.core.date.DateUtil;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.trace.IdGenerator;
import org.aries.middleware.hermes.common.utils.LongUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class HermesIdGenerator implements IdGenerator {
    private static long TODAY_MILLIS = 0L;
    private static final String PATTERN = "yyyyMMdd";
    private static int DAY_INT = 0;

    private final int instanceId;
    private final AtomicLong counter;

    HermesIdGenerator(int instanceId) {
        this.instanceId = instanceId;
        this.counter = new AtomicLong();
    }

    @Override
    public String generateSpanId() {
        long id;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        do {
            id = random.nextLong();
        } while (id == 0);
        return SpanId.fromLong(id);
    }

    @Override
    public String generateTraceId() {
        int dayStr = dayInt();
        return TraceId.fromLongs(LongUtil.combine(dayStr, instanceId), counter.getAndIncrement());
    }


    static synchronized int dayInt() {
        long now = System.currentTimeMillis();
        if (TODAY_MILLIS == 0) {
            Date date = new Date();
            TODAY_MILLIS = day(date);
            DAY_INT = Integer.parseInt(DateUtil.format(date, PATTERN));
            return DAY_INT;
        }

        if (now <= TODAY_MILLIS) {
            return DAY_INT;
        }

        Date date = new Date();
        TODAY_MILLIS = day(date);
        DAY_INT = Integer.parseInt(DateUtil.format(date, PATTERN));
        return DAY_INT;
    }

    static long day(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }


    public static IdGenerator of(int instanceId) {
        return new HermesIdGenerator(instanceId);
    }
}
