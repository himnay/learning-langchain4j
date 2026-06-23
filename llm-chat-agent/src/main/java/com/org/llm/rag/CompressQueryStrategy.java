package com.org.llm.rag;

import com.org.llm.model.QueryTransformRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Compresses conversation history plus the current query into a single standalone query.
 */
@Component
@RequiredArgsConstructor
class CompressQueryStrategy implements QueryTransformationStrategy {

    private final CompressingQueryTransformer compressingQueryTransformer;

    // Alternating turns, oldest first, starting with the user — see QueryTransformRequest#history
    private static List<ChatMessage> toHistory(List<String> turns) {
        List<ChatMessage> history = new ArrayList<>();
        if (turns == null) {
            return history;
        }
        for (int i = 0; i < turns.size(); i++) {
            history.add(i % 2 == 0 ? UserMessage.from(turns.get(i)) : AiMessage.from(turns.get(i)));
        }
        return history;
    }

    @Override
    public QueryTransformationTechnique technique() {
        return QueryTransformationTechnique.COMPRESS;
    }

    @Override
    public List<String> transform(QueryTransformRequest request) {
        UserMessage currentMessage = UserMessage.from(request.getQuery());
        Metadata metadata = Metadata.builder()
                .chatMessage(currentMessage)
                .chatMemory(toHistory(request.getHistory()))
                .build();
        Query query = new Query(request.getQuery(), metadata);
        return compressingQueryTransformer.transform(query).stream().map(Query::text).toList();
    }
}
