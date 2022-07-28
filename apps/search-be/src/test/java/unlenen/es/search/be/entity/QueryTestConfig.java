package unlenen.es.search.be.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class QueryTestConfig {
    @Value("${search.test.index}")
    String indexName;
}
