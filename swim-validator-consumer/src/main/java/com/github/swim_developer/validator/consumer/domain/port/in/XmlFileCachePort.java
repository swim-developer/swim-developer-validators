package com.github.swim_developer.validator.consumer.domain.port.in;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface XmlFileCachePort {
    void clearXmlCache();
    String readEventFile(String filename);
    List<Path> listXmlFiles() throws IOException;
    Path selectRandomFile(List<Path> files);
    List<String> getCachedXmlContent();
    String getEventsPath();
}
