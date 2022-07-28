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

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.util.ObjectBuilder;
import unlenen.es.search.be.entity.query.Statement;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
public abstract class AbstractOperator {

    public abstract Query createStatementQuery(Statement statement, boolean insideCondition);

    public abstract boolean isExclude(Statement statement);

    protected ObjectBuilder<FieldValue> getValue(Object value,
            co.elastic.clients.elasticsearch._types.FieldValue.Builder fieldValue) {
        if (value instanceof String)
            return fieldValue.stringValue(((String) value).toLowerCase());
        if (value instanceof Boolean)
            return fieldValue.booleanValue((Boolean) value);
        if (value instanceof Double)
            return fieldValue.doubleValue((Double) value);
        if (value instanceof Long)
            return fieldValue.longValue((Long) value);
        return null;
    }

}
