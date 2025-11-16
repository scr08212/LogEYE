package com.nkm.logeye.domain.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class AIProperties {
    private String apiKey;
    private String url;
}
