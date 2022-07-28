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
package unlenen.es.search.be.entity.query.transformers.query;

import java.util.ArrayList;
import java.util.List;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import unlenen.es.search.be.entity.query.Conditions;
import unlenen.es.search.be.entity.query.QueryItem;
import unlenen.es.search.be.entity.query.Statement;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
public class ConditionsQueryTransformer implements QueryItemTransformer {

    @Override
    /**
     * Creates a ES Bool Query
     */
    public Query createQuery(QueryItem queryItem, boolean insideCondition) {
        Conditions conditions = (Conditions) queryItem;
        return QueryBuilders.bool(fn -> createBoolQuery(fn, conditions));
    }

    /**
     * Creates bool query
     * if conditions object is an AND query 
     * @param boolQuery
     * @param conditions
     * @return
     */
    private Builder createBoolQuery(Builder boolQuery, Conditions conditions) {
        List<Query> mustQueries = new ArrayList<>();
        List<Query> mustNotQueries = new ArrayList<>();

        for (QueryItem item : conditions.getQueryItems()) {
            switch (item.getClass().getSimpleName()) {
                case "Statement": {
                    Statement statement = (Statement) item;
                    StatementQueryTransformer statementQuery = new StatementQueryTransformer();
                    Query query = statementQuery.createQuery(statement, true);
                    if (statementQuery.isExcludeStatement(statement)) {
                        mustNotQueries.add(query);
                    } else {
                        mustQueries.add(query);
                    }
                }
            }
        }

        switch (conditions.getConditionType()) {
            case AND: {
                if (!mustQueries.isEmpty()) {
                    boolQuery.must(mustQueries);
                }

                if (!mustNotQueries.isEmpty()) {
                    boolQuery.mustNot(mustNotQueries);
                }
                break;
            }
            case OR: {
                if (!mustQueries.isEmpty()) {
                    boolQuery.should(mustQueries);
                }

                if (!mustNotQueries.isEmpty()) {
                    boolQuery.mustNot(mustNotQueries);
                }
                break;
            }
        }

        return boolQuery;
    }
}
