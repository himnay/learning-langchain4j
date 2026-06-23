package com.org.llm.rag;

import com.org.llm.model.QueryTransformRequest;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Translates the query into the language of the indexed documents.
 *
 * <p>LangChain4j has no built-in translation query transformer — this reproduces Spring AI's
 * {@code TranslationQueryTransformer} with a direct prompt against the deterministic RAG chat
 * model, built fresh per request since the target language varies per call.</p>
 */
@Component
class TranslateQueryStrategy implements QueryTransformationStrategy {

    private static final String DEFAULT_TARGET_LANGUAGE = "English";

    private final ChatModel ragChatModel;

    TranslateQueryStrategy(@Qualifier("ragChatModel") ChatModel ragChatModel) {
        this.ragChatModel = ragChatModel;
    }

    @Override
    public QueryTransformationTechnique technique() {
        return QueryTransformationTechnique.TRANSLATE;
    }

    @Override
    public List<String> transform(QueryTransformRequest request) {
        String targetLanguage = request.getTargetLanguage() != null ? request.getTargetLanguage() : DEFAULT_TARGET_LANGUAGE;
        String prompt = """
                Translate the query below into %s. Return ONLY the translated query — no markdown,
                no explanation, no quotes.

                Query: %s
                """.formatted(targetLanguage, request.getQuery());
        return List.of(ragChatModel.chat(prompt).trim());
    }
}
