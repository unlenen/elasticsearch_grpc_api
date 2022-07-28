package unlenen.es.search.fe.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import unlenen.es.search.fe.config.SearchConfig;
import unlenen.es.search.fe.exception.QueryException;
import unlenen.es.search.proto.ConditionType;
import unlenen.es.search.proto.Conditions;
import unlenen.es.search.proto.Conditions.Builder;
import unlenen.es.search.proto.Operator;
import unlenen.es.search.proto.SearchQuery;
import unlenen.es.search.proto.Statement;

@Service
public class QueryParserService {

    @Autowired
    SearchConfig searchConfig;

    final String operatorRegex = "(=|<>|%|~)";

    /**
     * Parse query string
     * 
     * @param query
     * @return
     * @throws QueryException
     */
    public SearchQuery parseQuery(String query) throws QueryException {
        Builder conditionsBuilder = Conditions.newBuilder()
                .setConditionType(getConditionType(query));

        parseStatements(conditionsBuilder, query);

        Conditions conditions = conditionsBuilder.build();
        SearchQuery searchQuery = createSearchQuery(conditions);

        return searchQuery;
    }

    private SearchQuery createSearchQuery(Conditions conditions) {
        SearchQuery searchQuery = SearchQuery.newBuilder()
                .setIndex(searchConfig.getIndex())
                .setConditions(conditions)
                .setTtl(searchConfig.getTtl())
                .build();
        return searchQuery;
    }

    private void parseStatements(Builder conditionsBuilder, String query) throws QueryException {
        String[] statements;
        if (query.indexOf("and") > -1 || query.indexOf("or") > -1) { // is query contains multiple statement
            statements = query.split("(and|or)");
        } else {
            statements = new String[] { query }; // single query
        }

        Pattern OPERATOR_PATTERN = Pattern.compile(operatorRegex);
        Matcher operatorMatcher = OPERATOR_PATTERN.matcher(query);

        int count = 0;
        while (operatorMatcher.find()) {
            count++;
        }

        if (statements.length != count) {
            throw new QueryException("Not valid query[" + query
                    + "] . Valid Query -> key " + operatorRegex + " value and / or key " + operatorRegex
                    + " value ....");
        }

        for (String statementStr : statements) {

            statementStr = statementStr.replaceAll(" ", "");
            String[] statementData = statementStr.split(operatorRegex);
            if (statementData.length != 2) {
                throw new QueryException(
                        "Not a valid statement[" + statementStr + "] . Statement --> key " + operatorRegex
                                + " value . ");
            }

            Statement statement = Statement.newBuilder()
                    .setKey(statementData[0])
                    .setOperator(getOperator(statementStr))
                    .setValue(statementData[1])
                    .build();

            conditionsBuilder.addStatement(statement);
        }
    }

    private ConditionType getConditionType(String command) {
        if (command.indexOf("and") > -1) {
            return ConditionType.AND;
        } else if (command.indexOf("or") > -1) {
            return ConditionType.OR;
        }
        return ConditionType.AND;
    }

    private Operator getOperator(String statementStr) throws QueryException {
        if (statementStr.indexOf("=") > -1) {
            return Operator.EQUAL;
        } else if (statementStr.indexOf("<>") > -1) {
            return Operator.NOT_EQUAL;
        } else if (statementStr.indexOf("%") > -1) {
            return Operator.INCLUDE;
        } else if (statementStr.indexOf("~") > -1) {
            return Operator.REGEX;
        } else if (statementStr.indexOf(">=") > -1) {
            return Operator.GREATER_EQUAL;
        } else if (statementStr.indexOf("<=") > -1) {
            return Operator.LITTLE_EQUAL;
        } else if (statementStr.indexOf(">") > -1) {
            return Operator.GREATER_THAN;
        } else if (statementStr.indexOf("<") > -1) {
            return Operator.LITTLE_THAN;
        }
        throw new QueryException(
                "No valid operator in statement[" + statementStr
                        + "]. Valid operators are Equal : '=' , Not Equal :'<>' , Contains : '%' , Regex : '~'");
    }

}
