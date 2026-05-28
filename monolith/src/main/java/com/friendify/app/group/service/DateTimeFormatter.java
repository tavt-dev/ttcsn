package com.friendify.app.group.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

@Component
public class DateTimeFormatter {
    private final Map<Long, Function<Instant, String>> strategyMap = new LinkedHashMap<>();

    public DateTimeFormatter() {
        strategyMap.put(60L, this::formatInSeconds);
        strategyMap.put(3600L, this::formatInMinutes);
        strategyMap.put(86400L, this::formatInHours);
        strategyMap.put(Long.MAX_VALUE, this::formatInDate);
    }

    public String format(Instant instant) {
        if (instant == null) {
            return null;
        }

        long elapsedSeconds = ChronoUnit.SECONDS.between(instant, Instant.now());
        var strategy = strategyMap.entrySet().stream()
                .filter(entry -> elapsedSeconds < entry.getKey())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No strategy found for elapsed seconds: " + elapsedSeconds));

        return strategy.getValue().apply(instant);
    }

    private String formatInSeconds(Instant instant) {
        long elapsedSeconds = ChronoUnit.SECONDS.between(instant, Instant.now());
        return String.format("%s second(s) ago", elapsedSeconds);
    }

    private String formatInMinutes(Instant instant) {
        long elapsedMinutes = ChronoUnit.MINUTES.between(instant, Instant.now());
        return String.format("%s minute(s) ago", elapsedMinutes);
    }

    private String formatInHours(Instant instant) {
        long elapsedHours = ChronoUnit.HOURS.between(instant, Instant.now());
        return String.format("%s hour(s) ago", elapsedHours);
    }

    private String formatInDate(Instant instant) {
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(java.time.format.DateTimeFormatter.ISO_DATE);
    }
}
