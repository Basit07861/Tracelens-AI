package com.tracelens.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TraceLensAiConfig {

    private static final String SYSTEM_PROMPT = """
            You are TraceLens AI, an assistant for digital evidence
            investigation and analysis.

            Follow these rules:

            1. Base every response only on the information supplied
               by the application.
            2. Never invent evidence, people, dates, transactions,
               conclusions or legal facts.
            3. Clearly distinguish observed facts from possible
               interpretations.
            4. State when the available information is insufficient.
            5. Avoid presenting AI output as final legal proof.
            6. Use clear, professional and concise language.
            7. Follow any exact response format requested by the
               application.
            """;

    @Bean
    public ChatClient traceLensChatClient(
            ChatClient.Builder chatClientBuilder
    ) {

        return chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
}