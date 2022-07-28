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
package unlenen.es.search.be.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Getter;
import unlenen.es.search.be.entity.query.QueryItem;
import unlenen.es.search.be.entity.query.transformers.query.ConditionsQueryTransformer;
import unlenen.es.search.be.entity.query.transformers.query.QueryItemTransformer;
import unlenen.es.search.be.entity.query.transformers.query.StatementQueryTransformer;
import unlenen.es.search.be.exception.SearchException;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@Configuration
@Getter
@EnableScheduling
public class SearchConfig {
    @Value("${es.host}")
    String elasticSearchHost;

    @Value("${es.port}")
    int elasticSearchPort;

    @Value("${es.search.maxResult}")
    int elasticSearchMaxResultSize;

    @Bean
    public RestClient getRestClient() {
        return RestClient
                .builder(new HttpHost(this.elasticSearchHost, this.elasticSearchPort))
                .build();
    }

    public ElasticsearchTransport getElasticsearchTransport() {
        return new RestClientTransport(
                getRestClient(), new JacksonJsonpMapper());
    }

    @Bean
    public ElasticsearchClient getElasticsearchClient() {
        return new ElasticsearchClient(getElasticsearchTransport());
    }

    public QueryItemTransformer getQueryItemTranformer(QueryItem queryItem) throws SearchException {
        if (queryItem.getClass().getSimpleName().equals("Statement")) {
            return new StatementQueryTransformer();
        } else if (queryItem.getClass().getSimpleName().equals("Conditions")) {
            return new ConditionsQueryTransformer();
        }
        throw new SearchException("Unknown queryItem type : " + queryItem);
    }
}
