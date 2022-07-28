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
package unlenen.es.search.be.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.devh.boot.grpc.server.service.GrpcService;
import unlenen.es.search.be.entity.SearchResult;
import unlenen.es.search.be.entity.document.Machine;
import unlenen.es.search.be.entity.query.ConditionType;
import unlenen.es.search.be.entity.query.Conditions;
import unlenen.es.search.be.entity.query.Operator;
import unlenen.es.search.be.entity.query.QueryItem;
import unlenen.es.search.be.entity.query.SearchQuery;
import unlenen.es.search.be.entity.query.Statement;
import unlenen.es.search.be.service.CachedSearchService;
import unlenen.es.search.proto.Error;
import unlenen.es.search.proto.KeyValuePair;
import unlenen.es.search.proto.QueryStatistic;
import unlenen.es.search.proto.Result;
import unlenen.es.search.proto.Result.Builder;
import unlenen.es.search.proto.ResultType;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@GrpcService
public class SearchServiceGrpc extends unlenen.es.search.proto.SearchServiceGrpc.SearchServiceImplBase {

    Logger logger = LoggerFactory.getLogger(SearchServiceGrpc.class);

    @Autowired
    CachedSearchService searchService;

    /**
     * Entrypoint of GRPC SearchService.search calls
     * 
     * @param: request : GRPC SearchQuery which contains the name of index and query
     *                 conditions
     * @return List of found machines due to query
     */
    @Override
    public void search(unlenen.es.search.proto.SearchQuery request,
            io.grpc.stub.StreamObserver<unlenen.es.search.proto.SearchResult> responseObserver) {

        long beginTime = System.currentTimeMillis();

        // Prepare Internal SearchQuery
        SearchQuery searchQuery = createSearchQuery(request);
        List<unlenen.es.search.proto.Result> grpcResult = new ArrayList();
        ResultType resultType;

        Error error;
        QueryStatistic queryStatistic;
        try {
            // Search
            SearchResult searchResult = searchService.search(searchQuery, Machine.class, request.getTtl());

            // Prepare Result
            long begin = System.currentTimeMillis();
            for (Object object : searchResult.getResult()) {
                grpcResult.add(convertToGrpcResult(object));
            }
            resultType = searchResult.getResultType();
            error = Error.newBuilder().build();
            searchResult.getStatistics().setRequestPrepareTime(System.currentTimeMillis() - begin);

            // Prepare Statistics
            queryStatistic = QueryStatistic
                    .newBuilder()
                    .setSearchTime(searchResult.getStatistics().getSearchTime())
                    .setResponsePrepareTime(searchResult.getStatistics().getResponsePrepareTime())
                    .setRequestPrepareTime(searchResult.getStatistics().getRequestPrepareTime())
                    .build();
        } catch (Exception e) {
            // Prepare Fail Response
            resultType = ResultType.FAIL;
            error = Error.newBuilder()
                    .setCode(e.getClass().getSimpleName())
                    .setMessage(e.getMessage())
                    .build();
            queryStatistic = QueryStatistic.newBuilder().build();
        }

        createSearchResult(responseObserver, grpcResult, resultType, error, queryStatistic);
        if (logger.isInfoEnabled()) {
            logger.info("[search] key" + searchQuery.toString() + " , resultType:" + resultType + " , responseSize:"
                    + grpcResult.size() + "  ,ttl: " + request.getTtl() + ", error : " + error.getCode() + " in time : "
                    + (System.currentTimeMillis() - beginTime));
        }
    }

    private void createSearchResult(io.grpc.stub.StreamObserver<unlenen.es.search.proto.SearchResult> responseObserver,
            List<unlenen.es.search.proto.Result> grpcResult, ResultType resultType, Error error,
            QueryStatistic queryStatistic) {
        unlenen.es.search.proto.SearchResult searchResult = unlenen.es.search.proto.SearchResult.newBuilder()
                .addAllResult(grpcResult)
                .setResultType(resultType)
                .setError(error)
                .setStatistics(queryStatistic)
                .build();
        responseObserver.onNext(searchResult);
        responseObserver.onCompleted();
    }

    private SearchQuery createSearchQuery(unlenen.es.search.proto.SearchQuery request) {
        SearchQuery searchQuery = new SearchQuery(request.getIndex());
        if (request.getConditions().getStatementCount() == 1) {
            searchQuery.setQueryItem(convertToStatement(request.getConditions().getStatement(0)));
        } else {
            searchQuery.setQueryItem(convertToConditions(request.getConditions()));
        }
        return searchQuery;
    }

    /**
     * Converts GRPC Conditions to Internal Conditions
     * 
     * @param request
     * @return Internal Conditions
     */
    private QueryItem convertToConditions(unlenen.es.search.proto.Conditions request) {
        Conditions conditions = new Conditions(ConditionType.valueOf(request.getConditionType().name()));
        for (unlenen.es.search.proto.Statement statement : request.getStatementList()) {
            conditions.getQueryItems().add(convertToStatement(statement));
        }
        return conditions;
    }

    /**
     * Converts GRPC Statement to Internal Statement
     * 
     * @param request
     * @return Internal Statement
     */
    private QueryItem convertToStatement(unlenen.es.search.proto.Statement request) {
        Statement statement = new Statement();
        statement.setField(request.getKey());
        statement.setOperator(Operator.valueOf(request.getOperator().name()));
        statement.setValue(request.getValue());
        return statement;
    }

    /**
     * Converts Internal Machine to GRPC Result Objects
     * 
     * @param result
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    private Result convertToGrpcResult(Object result)
            throws IllegalArgumentException, IllegalAccessException {
        Builder builder = Result.newBuilder();
        for (Field field : result.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            builder.addKeyValuePair(createKeyPair(field.getName(), field.get(result) + ""));
        }
        return builder.build();
    }

    /**
     * Creates KeyValuePair from key and value
     * 
     * @param key
     * @param value
     * @return
     */
    private KeyValuePair createKeyPair(String key, String value) {
        return KeyValuePair.newBuilder()
                .setKey(key)
                .setValue(value)
                .build();
    }

}
