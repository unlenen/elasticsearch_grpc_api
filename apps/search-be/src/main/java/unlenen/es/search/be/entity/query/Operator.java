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
package unlenen.es.search.be.entity.query;

import lombok.Getter;
import unlenen.es.search.be.entity.query.transformers.operator.AbstractOperator;
import unlenen.es.search.be.entity.query.transformers.operator.OperatorEqual;
import unlenen.es.search.be.entity.query.transformers.operator.OperatorInclude;
import unlenen.es.search.be.entity.query.transformers.operator.OperatorNotEqual;
import unlenen.es.search.be.entity.query.transformers.operator.OperatorRegex;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@Getter
public enum Operator {
    EQUAL(OperatorEqual.class),
    NOT_EQUAL(OperatorNotEqual.class),
    INCLUDE(OperatorInclude.class),
    REGEX(OperatorRegex.class);

    Class<? extends AbstractOperator> operatorTranformer;

    Operator(Class<? extends AbstractOperator> operatorTranformer) {
        this.operatorTranformer = operatorTranformer;
    }

}
