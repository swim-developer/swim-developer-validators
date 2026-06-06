package com.github.swim_developer.validator.consumer.domain.port.in;

import com.github.swim_developer.validator.consumer.domain.model.ScenarioPreview;

public interface ScenarioPreviewPort {
    ScenarioPreview getDuplicatePreview();
    String getDuplicateXmlToSend();
    ScenarioPreview getMalformedPreview();
    String getMalformedXmlToSend();
    ScenarioPreview getMultipleMessagesPreview();
    String getMultipleMessagesToSend();
    ScenarioPreview getMultipleMessagesWithErrorPreview();
    String getMultipleMessagesWithErrorToSend();
}
