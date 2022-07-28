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
package unlenen.es.search.be.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import unlenen.es.search.be.Application;
import unlenen.es.search.be.entity.document.Machine;
import unlenen.es.search.be.entity.query.ConditionType;
import unlenen.es.search.be.entity.query.Conditions;
import unlenen.es.search.be.entity.query.Operator;
import unlenen.es.search.be.entity.query.SearchQuery;
import unlenen.es.search.be.entity.query.Statement;
import unlenen.es.search.be.service.ESSearchService;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class QueryTest {

    @Autowired
    QueryTestConfig config;

    @Autowired
    ESSearchService esSearchService;

    @Test
    /**
     * Test single statement for equal
     * region = earth
     */
    public void test_statement_equal() {
        String key = "region";
        String value = "earth";
        Statement statement = new Statement();
        statement.setField(key);
        statement.setOperator(Operator.EQUAL);
        statement.setValue(value);

        SearchQuery query = new SearchQuery(config.getIndexName());
        query.setQueryItem(statement);

        assertDoesNotThrow(() -> {
            List<Machine> machines = (List<Machine>) esSearchService.search(query, Machine.class).getResult();
            for (Machine machine : machines) {
                assert machine.getRegion().equals(value);
            }
            assert !machines.isEmpty() & machines.size() == 138;
        });
    }

    @Test
    /**
     * Test all fields are searchable with single statement
     * guest_os ~ *ento*
     */
    public void test_statement_check_all_fields() {
        String jsonData = "{\"id\":1000,\"machine_name\":\"32dfd06f68afea5bee27c973cff8cca7\",\"status\":\"running\",\"region\":\"venus\",\"federation\":\"lidya\",\"tags\":\"elasticsearch\",\"ip_address\":\"148.27.131.33\",\"team\":\"beholder\",\"guest_os\":\"Ubuntu\"}";
        assertDoesNotThrow(() -> {
            JSONObject jsonObject = new JSONObject(jsonData);
            Iterator iterator = jsonObject.keys();
            while (iterator.hasNext()) {

                String key = (String) iterator.next();
                String value = jsonObject.getString(key);
                System.out.println("test_statement_check_all_fields -> " + key + " = " + value);
                Statement statement = new Statement();
                statement.setField(key);
                statement.setOperator(Operator.EQUAL);
                statement.setValue(value);

                SearchQuery query = new SearchQuery(config.getIndexName());
                query.setQueryItem(statement);

                List results = (List<Machine>) esSearchService.search(query, Machine.class).getResult();
                assert !results.isEmpty();
            }
        });

    }

    @Test
    /**
     * Test single statement for include
     * guest_os ~ *ento*
     */
    public void test_statement_include() {
        String key = "guest_os";
        String value = "*ento*";
        String expectedValue = "Centos";
        Statement statement = new Statement();
        statement.setField(key);
        statement.setOperator(Operator.INCLUDE);
        statement.setValue(value);

        SearchQuery query = new SearchQuery(config.getIndexName());
        query.setQueryItem(statement);

        assertDoesNotThrow(() -> {
            List<Machine> machines = (List<Machine>) esSearchService.search(query, Machine.class).getResult();
            for (Machine machine : machines) {
                assert machine.getGuest_os().equals(expectedValue);
            }
            assert !machines.isEmpty() && machines.size() == 519;
        });
    }

    @Test
    /**
     * Test single statement for regex
     * guest_os ~ Cen[a-z]+
     */
    public void test_statement_regex() {
        String key = "guest_os";
        String value = "Cen[a-z]+";
        String expectedValue = "Centos";
        Statement statement = new Statement();
        statement.setField(key);
        statement.setOperator(Operator.REGEX);
        statement.setValue(value);

        SearchQuery query = new SearchQuery(config.getIndexName());
        query.setQueryItem(statement);

        assertDoesNotThrow(() -> {
            List<Machine> machines = (List<Machine>) esSearchService.search(query, Machine.class).getResult();
            for (Machine machine : machines) {
                assert machine.getGuest_os().equals(expectedValue);
            }
            assert !machines.isEmpty() && machines.size() == 519;
        });
    }

    @Test
    /**
     * Test single statement for not equal
     * guest_os <> Centos
     */
    public void test_statement_not_equal() {
        String key = "guest_os";
        String value = "Centos";
        Statement statement = new Statement();
        statement.setField(key);
        statement.setOperator(Operator.NOT_EQUAL);
        statement.setValue(value);

        SearchQuery query = new SearchQuery(config.getIndexName());
        query.setQueryItem(statement);

        assertDoesNotThrow(() -> {
            List<Machine> machines = (List<Machine>) esSearchService.search(query, Machine.class).getResult();
            for (Machine machine : machines) {
                assert !machine.getGuest_os().equals(value);
            }
            assert !machines.isEmpty() && machines.size() == 481;
        });
    }

    @Test
    /**
     * Tests Conditional Query with AND
     * guest_os like *ent* and region = mars and team != platform
     */
    public void test_condition_and() {

        String data = "[{\"name\":\"guest_os\",\"op\":\"INCLUDE\",\"value\":\"*ent*\"},{\"name\":\"region\",\"op\":\"EQUAL\",\"value\":\"mars\"},{\"name\":\"team\",\"op\":\"NOT_EQUAL\",\"value\":\"platform\"}]";
        assertDoesNotThrow(() -> {
            JSONArray jsonArray = new JSONArray(data);

            Conditions conditions = new Conditions(ConditionType.AND);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Statement statement = new Statement();
                statement.setField(obj.getString("name"));
                statement.setOperator(Operator.valueOf(obj.getString("op")));
                statement.setValue(obj.getString("value"));
                conditions.getQueryItems().add(statement);
            }

            SearchQuery query = new SearchQuery(config.getIndexName());
            query.setQueryItem(conditions);

            List<Machine> machines = (List<Machine>) esSearchService.search(query, Machine.class).getResult();

            for (Machine machine : machines) {
                assert machine.getGuest_os().equals("Centos") && machine.getRegion().equals("mars")
                        && !machine.getTeam().equals("platform");
            }
            assert !machines.isEmpty() && machines.size() == 59;
        });
    }

    @Test
    public void test_condition_and_all_fields() {
        String jsonData = "{\"id\":1000,\"machine_name\":\"32dfd06f68afea5bee27c973cff8cca7\",\"status\":\"running\",\"region\":\"venus\",\"federation\":\"lidya\",\"tags\":\"elasticsearch\",\"ip_address\":\"148.27.131.33\",\"team\":\"beholder\",\"guest_os\":\"Ubuntu\"}";
        assertDoesNotThrow(() -> {
            JSONObject jsonObject = new JSONObject(jsonData);

            Conditions conditions = new Conditions(ConditionType.AND);

            Iterator iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = jsonObject.getString(key);
                Statement statement = new Statement();
                statement.setField(key);
                statement.setOperator(Operator.EQUAL);
                statement.setValue(value);
                conditions.getQueryItems().add(statement);
            }

            SearchQuery query = new SearchQuery(config.getIndexName());
            query.setQueryItem(conditions);

            List<Machine> machines = (List<Machine>) esSearchService.search(query, Machine.class).getResult();

            for (Machine machine : machines) {
                assert machine.getId() == 1000;
            }
            assert !machines.isEmpty() && machines.size() == 1;
        });

    }

    @Test
    /**
     * Tests Conditional Query with OR
     * guest_os like *ent* and region = mars or region = mars
     */
    public void test_condition_or() {

        String data = "[{\"name\":\"guest_os\",\"op\":\"INCLUDE\",\"value\":\"*ent*\"},{\"name\":\"region\",\"op\":\"EQUAL\",\"value\":\"mars\"}]";
        assertDoesNotThrow(() -> {
            JSONArray jsonArray = new JSONArray(data);

            Conditions conditions = new Conditions(ConditionType.OR);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Statement statement = new Statement();
                statement.setField(obj.getString("name"));
                statement.setOperator(Operator.valueOf(obj.getString("op")));
                statement.setValue(obj.getString("value"));
                conditions.getQueryItems().add(statement);
            }

            SearchQuery query = new SearchQuery(config.getIndexName());
            query.setQueryItem(conditions);

            List<Machine> machines = (List<Machine>) esSearchService.search(query, Machine.class).getResult();
            for (Machine machine : machines) {
                assert machine.getGuest_os().equals("Centos") || machine.getRegion().equals("mars");
            }
            System.out.println(machines.size());
            assert !machines.isEmpty() && machines.size() == 590;
        });
    }
}
