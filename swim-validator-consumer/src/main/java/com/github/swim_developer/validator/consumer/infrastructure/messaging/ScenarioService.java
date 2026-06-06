package com.github.swim_developer.validator.consumer.infrastructure.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import com.github.swim_developer.validator.consumer.domain.port.in.ScenarioPreviewPort;
import com.github.swim_developer.validator.consumer.domain.port.in.XmlFileCachePort;
import com.github.swim_developer.validator.consumer.domain.model.ScenarioPreview;

@Slf4j
@ApplicationScoped
public class ScenarioService implements ScenarioPreviewPort {

    private static final String XML_MESSAGES_OPEN = "<messages>\n";
    private static final String XML_MESSAGE_WRAPPER_START = "  <message type=\"text\">\n    <body>\n      <![CDATA[\n";
    private static final String XML_MESSAGE_WRAPPER_END = "\n      ]]>\n    </body>\n  </message>\n";
    private static final String XML_MESSAGES_CLOSE = "</messages>";
    private static final Pattern MESSAGE_CONTENT_PATTERN = Pattern.compile(
            "<message[^>]*>\\s*<body>\\s*<!\\[CDATA\\[(.+?)]]>\\s*</body>\\s*</message>",
            Pattern.DOTALL);

    private final XmlFileCachePort xmlEventFileLoader;

    @Inject
    public ScenarioService(XmlFileCachePort xmlEventFileLoader) {
        this.xmlEventFileLoader = xmlEventFileLoader;
    }

    public ScenarioPreview getMalformedPreview() {
        String randomXml = getRandomXmlFromCache();
        String content = extractMessageContent(randomXml);
        String brokenContent = breakXml(content);
        String wrappedXml = wrapInMessages(brokenContent);
        return new ScenarioPreview(wrappedXml, "A closing tag was removed to make this XML malformed.");
    }

    public ScenarioPreview getDuplicatePreview() {
        String randomXml = getRandomXmlFromCache();
        return new ScenarioPreview(randomXml, "This exact message will be sent twice without date randomization to test duplicate detection.");
    }

    public ScenarioPreview getMultipleMessagesPreview() {
        List<String> xmlCache = xmlEventFileLoader.getCachedXmlContent();
        if (xmlCache.size() < 2) {
            return new ScenarioPreview("<messages><message>Not enough messages in cache</message></messages>", "Need at least 2 messages.");
        }

        int count = Math.min(3, xmlCache.size());
        StringBuilder combined = new StringBuilder(XML_MESSAGES_OPEN);
        for (int i = 0; i < count; i++) {
            String xml = xmlCache.get(ThreadLocalRandom.current().nextInt(xmlCache.size()));
            String content = extractMessageContent(xml);
            combined.append(XML_MESSAGE_WRAPPER_START);
            combined.append(content);
            combined.append(XML_MESSAGE_WRAPPER_END);
        }
        combined.append(XML_MESSAGES_CLOSE);
        return new ScenarioPreview(combined.toString(), count + " messages combined into a single payload.");
    }

    public ScenarioPreview getMultipleMessagesWithErrorPreview() {
        List<String> xmlCache = xmlEventFileLoader.getCachedXmlContent();
        if (xmlCache.size() < 2) {
            return new ScenarioPreview("<messages><message>Not enough messages in cache</message></messages>", "Need at least 2 messages.");
        }

        int count = Math.min(3, xmlCache.size());
        int errorIndex = ThreadLocalRandom.current().nextInt(count);
        StringBuilder combined = new StringBuilder(XML_MESSAGES_OPEN);

        for (int i = 0; i < count; i++) {
            String xml = xmlCache.get(ThreadLocalRandom.current().nextInt(xmlCache.size()));
            String content = extractMessageContent(xml);
            if (i == errorIndex) {
                content = breakXml(content);
            }
            combined.append(XML_MESSAGE_WRAPPER_START);
            combined.append(content);
            combined.append(XML_MESSAGE_WRAPPER_END);
        }
        combined.append(XML_MESSAGES_CLOSE);
        return new ScenarioPreview(combined.toString(), count + " messages combined. Message #" + (errorIndex + 1) + " contains an intentional error.");
    }

    public String getMalformedXmlToSend() {
        String randomXml = getRandomXmlFromCache();
        String content = extractMessageContent(randomXml);
        String brokenContent = breakXmlForSending(content);
        return wrapInMessages(brokenContent);
    }

    public String getDuplicateXmlToSend() {
        return getRandomXmlFromCache();
    }

    public String getMultipleMessagesToSend() {
        List<String> xmlCache = xmlEventFileLoader.getCachedXmlContent();
        int count = Math.min(3, xmlCache.size());
        StringBuilder combined = new StringBuilder(XML_MESSAGES_OPEN);
        for (int i = 0; i < count; i++) {
            String xml = xmlCache.get(ThreadLocalRandom.current().nextInt(xmlCache.size()));
            String content = extractMessageContent(xml);
            combined.append(XML_MESSAGE_WRAPPER_START);
            combined.append(content);
            combined.append(XML_MESSAGE_WRAPPER_END);
        }
        combined.append(XML_MESSAGES_CLOSE);
        return combined.toString();
    }

    public String getMultipleMessagesWithErrorToSend() {
        List<String> xmlCache = xmlEventFileLoader.getCachedXmlContent();
        int count = Math.min(3, xmlCache.size());
        int errorIndex = ThreadLocalRandom.current().nextInt(count);
        StringBuilder combined = new StringBuilder(XML_MESSAGES_OPEN);
        for (int i = 0; i < count; i++) {
            String xml = xmlCache.get(ThreadLocalRandom.current().nextInt(xmlCache.size()));
            String content = extractMessageContent(xml);
            if (i == errorIndex) {
                content = breakXmlForSending(content);
            }
            combined.append(XML_MESSAGE_WRAPPER_START);
            combined.append(content);
            combined.append(XML_MESSAGE_WRAPPER_END);
        }
        combined.append(XML_MESSAGES_CLOSE);
        return combined.toString();
    }

    private String getRandomXmlFromCache() {
        List<String> xmlCache = xmlEventFileLoader.getCachedXmlContent();
        if (xmlCache.isEmpty()) {
            throw new IllegalStateException("No XML files in cache");
        }
        return xmlCache.get(ThreadLocalRandom.current().nextInt(xmlCache.size()));
    }

    private String extractMessageContent(String xml) {
        Matcher matcher = MESSAGE_CONTENT_PATTERN.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return xml;
    }

    private String breakXml(String xml) {
        int lastClosingTag = xml.lastIndexOf("</");
        if (lastClosingTag > 0) {
            int tagEnd = xml.indexOf(">", lastClosingTag);
            if (tagEnd > 0) {
                return xml.substring(0, lastClosingTag) + ScenarioPreview.BREAK_MARKER + xml.substring(tagEnd + 1);
            }
        }
        return xml + "\n" + ScenarioPreview.BREAK_MARKER;
    }

    private String breakXmlForSending(String xml) {
        int lastClosingTag = xml.lastIndexOf("</");
        if (lastClosingTag > 0) {
            int tagEnd = xml.indexOf(">", lastClosingTag);
            if (tagEnd > 0) {
                return xml.substring(0, lastClosingTag) + xml.substring(tagEnd + 1);
            }
        }
        return xml;
    }

    private String wrapInMessages(String content) {
        return XML_MESSAGES_OPEN + XML_MESSAGE_WRAPPER_START + content + XML_MESSAGE_WRAPPER_END + XML_MESSAGES_CLOSE;
    }
}
