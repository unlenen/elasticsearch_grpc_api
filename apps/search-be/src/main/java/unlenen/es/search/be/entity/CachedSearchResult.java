package unlenen.es.search.be.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CachedSearchResult {

    long createTime;
    long ttl;
    SearchResult searchResult;

    public CachedSearchResult(SearchResult searchResult, long ttl) {
        this.searchResult = searchResult;
        this.createTime = System.currentTimeMillis();
        this.ttl = ttl;
    }

}
