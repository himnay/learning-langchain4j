package com.org.llm.model;

/** Result of persisting an uploaded audio file to the local audio working directory. */
public record StoredAudio(
        String fileId,
        String originalFilename,
        String storedFileName,
        String contentType,
        long size
) {
}
