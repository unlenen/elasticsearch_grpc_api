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
package unlenen.es.search.be.entity.query.transformers.operator;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import unlenen.es.search.be.entity.query.Statement;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
public class OperatorEqual extends AbstractOperator {

    @Override
    public Query createStatementQuery(Statement statement, boolean insideCondition) {
        return QueryBuilders.term(
                fn -> fn.field(statement.getField()).value(fieldValue -> getValue(statement.getValue(), fieldValue)));
    }

    @Override
    public boolean isExclude(Statement statement) {
        return false;
    }

}
