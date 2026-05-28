package com.friendify.app.post.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

@Component("postDateTimeFormatter")
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
        return String.format("%s second(s) ago", ChronoUnit.SECONDS.between(instant, Instant.now()));
    }

    private String formatInMinutes(Instant instant) {
        return String.format("%s minute(s) ago", ChronoUnit.MINUTES.between(instant, Instant.now()));
    }

    private String formatInHours(Instant instant) {
        return String.format("%s hour(s) ago", ChronoUnit.HOURS.between(instant, Instant.now()));
    }

    private String formatInDate(Instant instant) {
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.format(java.time.format.DateTimeFormatter.ISO_DATE);
    }
}
