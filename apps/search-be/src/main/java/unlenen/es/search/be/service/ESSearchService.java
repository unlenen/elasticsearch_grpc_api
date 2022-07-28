/*
# Copyright © 2022 Nebi Volkan UNLENEN
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
 */
package unlenen.es.search.be.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest.Builder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import unlenen.es.search.be.config.SearchConfig;
import unlenen.es.search.be.entity.SearchResult;
import unlenen.es.search.be.entity.query.QueryItem;
import unlenen.es.search.be.entity.query.SearchQuery;
import unlenen.es.search.be.entity.query.transformers.query.QueryItemTransformer;
import unlenen.es.search.be.exception.SearchException;
import unlenen.es.search.proto.ResultType;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@Service
public class ESSearchService {

    Logger logger = LoggerFactory.getLogger(ESSearchService.class);

    @Autowired
    ElasticsearchClient client;

    @Autowired
    SearchConfig config;

    /**
     * Search Query from ElasticSearch and returns found entities with max 10K
     * 
     * @param searchQuery   : Query Definition
     * @param documentClass : Result Class Type
     * @return : List of found entities for query
     * @throws SearchException : When query is not valid or ElasticSearch Connection
     *                         Problems
     */
    public SearchResult search(SearchQuery searchQuery, Class documentClass) throws SearchException {
        SearchResult searchResult = new SearchResult(new ArrayList());

        try {

            long begin = System.currentTimeMillis();
            SearchRequest searchRequest = generateSearchRequest(searchQuery).build();
            searchResult.getStatistics().setRequestPrepareTime(System.currentTimeMillis() - begin);

            begin = System.currentTimeMillis();
            SearchResponse search = client.search(searchRequest, documentClass);
            searchResult.getStatistics().setSearchTime(System.currentTimeMillis() - begin);

            ((List<Hit>) search.hits().hits()).stream().forEach(hit -> searchResult.getResult().add(hit.source()));
            searchResult.setResultType(ResultType.SUCCESS_NORMAL);

            if (logger.isDebugEnabled()) {
                logger.debug("[search] key:" + searchQuery.toString() + " , resultSize:"
                        + searchResult.getResult().size() + " in " + searchResult.getStatistics().getSearchTime());
            }
            return searchResult;
        } catch (ElasticsearchException | IOException | SearchException e) {
            if (e instanceof SearchException) {
                throw (SearchException) e;
            }
            throw new SearchException(e.getMessage(), e);
        }
    }

    /**
     * Generates SearchRequest for given SearchQuery
     * 
     * @param searchQuery   : Query Definition
     * @param searchRequest : Builder
     * @return :
     * @throws SearchException
     */
    private Builder generateSearchRequest(SearchQuery searchQuery) throws SearchException {
        SearchRequest.Builder searchRequest = new SearchRequest.Builder();
        searchRequest.from(0);
        searchRequest.size(config.getElasticSearchMaxResultSize());
        return searchRequest.index(searchQuery.getIndex()).query(generateQuery(searchQuery));
    }

    /**
     * Finds correct transformer for Query Object and transforms to ElasticSearch
     * Search API Query Type
     * 
     * @param searchQuery : Query Definition
     * @return : ElasticSearch Query object
     * @throws SearchException : When query definition is not valid
     */
    private Query generateQuery(SearchQuery searchQuery) throws SearchException {
        QueryItem queryItem = searchQuery.getQueryItem();
        QueryItemTransformer queryItemTranformer = config.getQueryItemTranformer(queryItem);
        return queryItemTranformer.createQuery(queryItem, false);
    }
}
