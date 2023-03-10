package com.boriselec.rimworld.aiart;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public record Counters(
        Counter rsImage,
        Counter rsQueued,
        Counter rsLimit,
        Counter translatedChars) {
    public Counters(MeterRegistry registry) {
        this(
                registry.counter("rs.image"),
                registry.counter("rs.queued"),
                registry.counter("rs.limit"),
                registry.counter("translate.chars." + timestamp()));
    }

    private static String timestamp() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
    }
}
