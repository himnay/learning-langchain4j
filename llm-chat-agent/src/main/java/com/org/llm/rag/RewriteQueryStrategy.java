package com.org.llm.rag;

import com.org.llm.model.QueryTransformRequest;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LLM rewrites a messy/conversational query into a clean, standalone search query.
 *
 * <p>LangChain4j ships no rewrite-specific transformer (only {@code CompressingQueryTransformer}
 * and {@code ExpandingQueryTransformer}) — this reproduces Spring AI's
 * {@code RewriteQueryTransformer} behavior with a direct prompt against the deterministic RAG
 * chat model ({@code RagConfig#ragChatModel}, temperature 0.0).</p>
 */
@Component
class RewriteQueryStrategy implements QueryTransformationStrategy {

    private static final String TARGET_SEARCH_SYSTEM =
            "a vector store holding corporate travel-policy and events documents";

    private final ChatModel ragChatModel;

    RewriteQueryStrategy(@Qualifier("ragChatModel") ChatModel ragChatModel) {
        this.ragChatModel = ragChatModel;
    }

    @Override
    public QueryTransformationTechnique technique() {
        return QueryTransformationTechnique.REWRITE;
    }

    @Override
    public List<String> transform(QueryTransformRequest request) {
        String prompt = """
                Rewrite the query below into a clean, standalone search query suitable for %s.
                Remove conversational filler, keep every important search term, and return ONLY
                the rewritten query — no markdown, no explanation.
                
                Query: %s
                """.formatted(TARGET_SEARCH_SYSTEM, request.getQuery());
        return List.of(ragChatModel.chat(prompt).trim());
    }
}
