package com.github.swim_developer.validator.core.infrastructure.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class XmlEd254DateRandomizer {

    private static final Pattern CREATION_TIME_ELEM = Pattern.compile("<creationTime>[^<]+</creationTime>");
    private static final Pattern PUBLICATION_TIME_ELEM = Pattern.compile("<publicationTime>[^<]+</publicationTime>");
    private static final Pattern AMAN_TARGET_LANDING = Pattern.compile("<amanTargetLandingTime>[^<]+</amanTargetLandingTime>");
    private static final Pattern AMAN_TARGET_TIME_OVER = Pattern.compile("<amanTargetTimeOver>[^<]+</amanTargetTimeOver>");
    private static final Pattern CREATION_TIME_ATTR = Pattern.compile("creationTime=\"[^\"]+\"");

    public String applyCurrentUtcDates(String xmlContent) {
        return replaceDateFields(xmlContent, Instant.now());
    }

    public String randomizeDates(String xmlContent) {
        return replaceDateFields(xmlContent, XmlDateRandomizer.randomRecentInstant());
    }

    private String replaceDateFields(String xmlContent, Instant baseTime) {
        String isoBase = XmlDateRandomizer.ISO_FORMATTER.format(baseTime);
        String isoBaseMillis = XmlDateRandomizer.ISO_MILLIS_FORMATTER.format(baseTime.plusSeconds(1));

        String out = replaceTag(CREATION_TIME_ELEM, xmlContent, "creationTime", isoBase);
        out = replaceTag(PUBLICATION_TIME_ELEM, out, "publicationTime", isoBaseMillis);

        Instant landingBase = baseTime.plus(20, ChronoUnit.MINUTES);
        Instant meteringBase = baseTime.minus(5, ChronoUnit.MINUTES);

        out = AMAN_TARGET_LANDING.matcher(out).replaceAll(m -> {
            Instant t = landingBase.plus(ThreadLocalRandom.current().nextInt(0, 40), ChronoUnit.MINUTES);
            return Matcher.quoteReplacement("<amanTargetLandingTime>" + XmlDateRandomizer.ISO_FORMATTER.format(t) + "</amanTargetLandingTime>");
        });

        out = AMAN_TARGET_TIME_OVER.matcher(out).replaceAll(m -> {
            Instant t = meteringBase.plus(ThreadLocalRandom.current().nextInt(0, 30), ChronoUnit.MINUTES);
            return Matcher.quoteReplacement("<amanTargetTimeOver>" + XmlDateRandomizer.ISO_FORMATTER.format(t) + "</amanTargetTimeOver>");
        });

        Instant gufiBase = baseTime.minus(ThreadLocalRandom.current().nextInt(2, 12), ChronoUnit.HOURS);
        out = CREATION_TIME_ATTR.matcher(out).replaceAll(m -> {
            Instant t = gufiBase.minus(ThreadLocalRandom.current().nextInt(0, 180), ChronoUnit.MINUTES);
            return Matcher.quoteReplacement("creationTime=\"" + XmlDateRandomizer.ISO_FORMATTER.format(t) + "\"");
        });

        return out;
    }

    private static String replaceTag(Pattern pattern, String input, String localName, String value) {
        return pattern.matcher(input)
                .replaceAll(Matcher.quoteReplacement("<" + localName + ">" + value + "</" + localName + ">"));
    }

}
