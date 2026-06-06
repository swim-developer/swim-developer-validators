package com.github.swim_developer.validator.core.infrastructure.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class XmlDateRandomizer {

    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter ISO_MILLIS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter COMPACT_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmm")
            .withZone(ZoneOffset.UTC);

    private static final Pattern BEGIN_POS = Pattern.compile("<gml:beginPosition>[^<]+</gml:beginPosition>");
    private static final Pattern END_POS = Pattern.compile("<gml:endPosition>[^<]+</gml:endPosition>");
    private static final Pattern EFFECTIVE_START = Pattern.compile("<event:effectiveStart>[^<]+</event:effectiveStart>");
    private static final Pattern EFFECTIVE_END = Pattern.compile("<event:effectiveEnd>[^<]+</event:effectiveEnd>");
    private static final Pattern YEAR = Pattern.compile("<event:year>[^<]+</event:year>");

    public String applyCurrentUtcDates(String xmlContent) {
        Instant start = Instant.now();
        Instant end = start.plus(1, ChronoUnit.DAYS);
        return replaceDateFields(xmlContent, start, end);
    }

    public String randomizeDates(String xmlContent) {
        Instant start = randomRecentInstant();
        Instant end = start.plus(ThreadLocalRandom.current().nextInt(1, 14), ChronoUnit.DAYS);
        return replaceDateFields(xmlContent, start, end);
    }

    public static Instant randomRecentInstant() {
        long now = System.currentTimeMillis();
        long days = ThreadLocalRandom.current().nextInt(0, 60);
        long offsetMs = ThreadLocalRandom.current().nextLong(0, 86400000L);
        return Instant.ofEpochMilli(now - days * 86400000L + offsetMs);
    }

    private String replaceDateFields(String xmlContent, Instant start, Instant end) {
        String isoBegin = ISO_FORMATTER.format(start);
        String isoEnd = ISO_FORMATTER.format(end);
        String compactBegin = COMPACT_FORMATTER.format(start);
        String compactEnd = COMPACT_FORMATTER.format(end);
        String year = String.valueOf(start.atZone(ZoneOffset.UTC).getYear());

        String out = replaceTag(BEGIN_POS, xmlContent, "gml:beginPosition", isoBegin);
        out = replaceTag(END_POS, out, "gml:endPosition", isoEnd);
        out = replaceTag(EFFECTIVE_START, out, "event:effectiveStart", compactBegin);
        out = replaceTag(EFFECTIVE_END, out, "event:effectiveEnd", compactEnd);
        out = replaceTag(YEAR, out, "event:year", year);
        return out;
    }

    private static String replaceTag(Pattern pattern, String input, String localName, String value) {
        return pattern.matcher(input)
                .replaceAll(Matcher.quoteReplacement("<" + localName + ">" + value + "</" + localName + ">"));
    }
}
