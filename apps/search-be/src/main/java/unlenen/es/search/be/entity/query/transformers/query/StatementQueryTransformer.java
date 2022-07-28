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

import java.lang.reflect.InvocationTargetException;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import unlenen.es.search.be.entity.query.QueryItem;
import unlenen.es.search.be.entity.query.Statement;
import unlenen.es.search.be.entity.query.transformers.operator.AbstractOperator;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
public class StatementQueryTransformer implements QueryItemTransformer {

    @Override
    public Query createQuery(QueryItem queryItem, boolean insideCondition) {
        Statement statement = (Statement) queryItem;
        return getOperatorTranformer(statement).createStatementQuery(statement, insideCondition);
    }

    public boolean isExcludeStatement(Statement statement) {
        return getOperatorTranformer(statement).isExclude(statement);
    }

    private AbstractOperator getOperatorTranformer(Statement statement) {
        Class<? extends AbstractOperator> transformerClass = statement.getOperator().getOperatorTranformer();
        AbstractOperator abstractOperator;
        try {
            abstractOperator = transformerClass.getDeclaredConstructor().newInstance();
            return abstractOperator;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
}
