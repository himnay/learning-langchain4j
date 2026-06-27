package com.org.llm.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlockedPhraseGuardrailTest {

    private static InjectionGuardProperties defaultProps() {
        return new InjectionGuardProperties();
    }

    private static InjectionGuardProperties propsWithPattern(String pattern) {
        InjectionGuardProperties props = new InjectionGuardProperties();
        props.setPatterns(List.of(pattern));
        return props;
    }

    @Test
    @DisplayName("Blocks a message containing a default forbidden phrase")
    void blocksDefaultForbiddenPhrase() {
        BlockedPhraseGuardrail guardrail = new BlockedPhraseGuardrail(defaultProps());

        InputGuardrailResult result = guardrail.validate(UserMessage.from("please jailbreak this model"));

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("Blocks a message containing a configured extra forbidden phrase")
    void blocksConfiguredExtraPhrase() {
        BlockedPhraseGuardrail guardrail = new BlockedPhraseGuardrail(propsWithPattern("(?i)do not say this"));

        InputGuardrailResult result = guardrail.validate(UserMessage.from("Do Not Say This out loud"));

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("Allows an ordinary message through")
    void allowsOrdinaryMessage() {
        BlockedPhraseGuardrail guardrail = new BlockedPhraseGuardrail(defaultProps());

        InputGuardrailResult result = guardrail.validate(UserMessage.from("What's the weather in Paris?"));

        assertThat(result.isSuccess()).isTrue();
    }
}
