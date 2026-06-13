package com.org.llm.validation;

import com.org.llm.exception.AudioValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AudioValidatorTest {

    private final AudioValidator validator = new AudioValidator();

    @Test
    void acceptsSupportedAudioType() {
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp3", "audio/mpeg", new byte[]{1});
        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "clip.mp3", "audio/mpeg", new byte[0]);
        assertThatExceptionOfType(AudioValidationException.class).isThrownBy(() -> validator.validate(file));
    }

    @Test
    void rejectsUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[]{1});
        assertThatExceptionOfType(AudioValidationException.class).isThrownBy(() -> validator.validate(file));
    }
}
