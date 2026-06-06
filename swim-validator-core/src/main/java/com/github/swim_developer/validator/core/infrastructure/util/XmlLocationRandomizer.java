package com.github.swim_developer.validator.core.infrastructure.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class XmlLocationRandomizer {

    private static final double LAT_MIN = 35.0;
    private static final double LAT_MAX = 70.0;
    private static final double LON_MIN = -10.0;
    private static final double LON_MAX = 40.0;

    private static final Pattern GML_POS = Pattern.compile("(<gml:pos[^>]*>)([^<]+)(</gml:pos>)");
    private static final Pattern EVENT_COORDS = Pattern.compile("(<event:coordinates>)([^<]+)(</event:coordinates>)");

    public String randomizeLocation(String xmlContent) {
        double lat = LAT_MIN + ThreadLocalRandom.current().nextDouble() * (LAT_MAX - LAT_MIN);
        double lon = LON_MIN + ThreadLocalRandom.current().nextDouble() * (LON_MAX - LON_MIN);
        String pos = String.format(java.util.Locale.US, "%.6f %.6f", lat, lon);
        String out = replaceGroup(GML_POS, xmlContent, pos);
        String av = toAviationCoordinateString(lat, lon);
        if (EVENT_COORDS.matcher(out).find()) {
            return replaceGroup(EVENT_COORDS, out, av);
        }
        return injectAfterLocation(out, av);
    }

    private static String injectAfterLocation(String xmlContent, String coordinates) {
        return xmlContent.replaceFirst(
            "(</event:location>)",
            "$1<event:coordinates>" + coordinates + "</event:coordinates>"
        );
    }

    private static String replaceGroup(Pattern pattern, String input, String inner) {
        Matcher m = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + inner + m.group(3)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String toAviationCoordinateString(double lat, double lon) {
        char ns = lat >= 0 ? 'N' : 'S';
        char ew = lon >= 0 ? 'E' : 'W';
        double alat = Math.abs(lat);
        double alon = Math.abs(lon);
        int latDeg = (int) Math.floor(alat);
        int latMin = (int) Math.floor((alat - latDeg) * 60);
        int lonDeg = (int) Math.floor(alon);
        int lonMin = (int) Math.floor((alon - lonDeg) * 60);
        return String.format(java.util.Locale.US, "%02d%02d%c%03d%02d%c", latDeg, latMin, ns, lonDeg, lonMin, ew);
    }
}
