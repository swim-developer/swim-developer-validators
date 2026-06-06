package com.github.swim_developer.validator.core.domain.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XmlPatternExtractor {

    private XmlPatternExtractor() {}

    public static Optional<String> extractFirst(Pattern pattern, String content) {
        Matcher m = pattern.matcher(content);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    public static List<String> extractAll(Pattern pattern, String content) {
        Matcher m = pattern.matcher(content);
        List<String> out = new ArrayList<>();
        while (m.find()) {
            out.add(m.group(1));
        }
        return out;
    }

    public static int countMatches(Pattern pattern, String content) {
        Matcher m = pattern.matcher(content);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }
}
