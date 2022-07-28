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
package unlenen.es.search.fe.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import unlenen.es.search.fe.config.SearchConfig;
import unlenen.es.search.fe.exception.QueryException;
import unlenen.es.search.fe.exception.SearchException;
import unlenen.es.search.fe.service.QueryParserService;
import unlenen.es.search.fe.service.SearchService;
import unlenen.es.search.proto.Result;
import unlenen.es.search.proto.ResultType;
import unlenen.es.search.proto.SearchQuery;
import unlenen.es.search.proto.SearchResult;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@Service
public class MainView {

    @Autowired
    SearchService searchService;

    @Autowired
    QueryParserService parser;

    @Autowired
    SearchConfig searchConfig;

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Entrypoint of MainView
     */
    public void start() {
        System.out.println("");
        System.out.println("********************************************************************");
        System.out.println("Welcome to Unlenen Elasticsearch query tool");
        System.out.println("********************************************************************");
        setIndex();
        printHelp();
        readCommand();
    }

    private void setIndex() {
        System.out.println("Please write the name of index you want to use");
        System.out.print("Index name(default:" + searchConfig.getDefaultIndex() + ")> ");
        String indexName = readLine();
        if ("".equals(indexName.trim())) {
            indexName = searchConfig.getDefaultIndex();
        }
        searchConfig.setIndex(indexName);
    }

    private void printHelp() {
        System.out.println("Usage:");
        System.out.println("    exit : Exit from application");
        System.out.println("    help: This Menu");
        System.out.println("    query-string : Search from elasticsearch with given query");
        System.out.println("Operators:");
        System.out.println("    Equal : '=' , Not Equal :'<>' , Contains : '%' , Regex : '~'");
        System.out.println("Multiple Statement");
        System.out.println("    and / or");
        System.out.println("Allowed Fields:");
        System.out
                .println(Arrays.asList(searchConfig.getUiHeaders()));
        System.out.println("Examples:");
        System.out.println("    region = earth and guest_os <> Centos and team % db* and tags ~ kube[a-z]+");
        System.out.println("    region = earth or guest_os <> Ubuntu");
        System.out.println("------------------------------------------------------------------");
    }

    /**
     * Reads command , process and display results
     */
    public void readCommand() {
        while (true) {
            System.out.print("query> ");
            try {
                String query = readLine();
                switch (query) {
                    case "exit": {
                        System.exit(0);
                    }
                    case "help": {
                        printHelp();
                        continue;
                    }
                }
                SearchQuery searchQuery = parser.parseQuery(query);
                long startTime = System.currentTimeMillis();
                SearchResult searchResult = searchService.search(searchQuery);
                long endTime = System.currentTimeMillis();
                printResult(searchResult, endTime - startTime);
            } catch (SearchException e) {
                System.out.println("Can not connect to backend");
            } catch (QueryException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }
    }

    private void printResult(SearchResult searchResult, long callTime) {

        if (searchResult.getResultType() == ResultType.FAIL) {
            System.out.println("Backend is failed:");
            System.out.println("    Error Code: " + searchResult.getError().getCode());
            System.out.println("    Error Message: " + searchResult.getError().getMessage());
            return;
        }
        List<Result> results = searchResult.getResultList();
        if (results.isEmpty()) {
            System.out.println("No result found for your query");
            return;
        }

        printSuccessResult(results);
        System.out.println(
                "Query is completed. "
                        + "Call:" + getSecond(callTime) + " ,"
                        + "esTime:" + getSecond(searchResult.getStatistics().getSearchTime()) + " seconds."
                        + " Total result are " + searchResult.getResultCount());
    }

    private double getSecond(long time) {
        return ((double) time) / 1000;
    }

    private void printSuccessResult(List<Result> results) {
        TableView st = new TableView();
        Result firstResult = results.get(0);
        List<String> headers = new ArrayList();
        firstResult.getKeyValuePairList().stream().forEach(t -> headers.add(t.getKey()));
        st.setHeaders(headers.toArray(new String[0]));

        st.setShowVerticalLines(true);

        for (Result result : results) {
            List<String> row = new ArrayList();
            result.getKeyValuePairList().stream().forEach(t -> row.add(t.getValue()));
            st.addRow(row.toArray(new String[0]));
        }

        st.print();
    }

    private String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
