package com.org.llm.rag;

import com.org.llm.model.QueryTransformRequest;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.transformer.ExpandingQueryTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generates several paraphrased variants of the query to improve recall on ambiguous questions.
 */
@Component
@RequiredArgsConstructor
class MultiQueryExpansionStrategy implements QueryTransformationStrategy {

    private final ExpandingQueryTransformer expandingQueryTransformer;

    @Override
    public QueryTransformationTechnique technique() {
        return QueryTransformationTechnique.MULTI_QUERY_EXPAND;
    }

    @Override
    public List<String> transform(QueryTransformRequest request) {
        return expandingQueryTransformer.transform(Query.from(request.getQuery())).stream()
                .map(Query::text)
                .toList();
    }
}
