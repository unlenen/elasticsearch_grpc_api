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
package unlenen.es.search.be.service;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import unlenen.es.search.be.entity.CachedSearchResult;
import unlenen.es.search.be.entity.SearchResult;
import unlenen.es.search.be.entity.query.SearchQuery;
import unlenen.es.search.proto.ResultType;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@Service
public class CachedSearchService {

    Logger logger = LoggerFactory.getLogger(CachedSearchService.class);

    ConcurrentHashMap<String, CachedSearchResult> caches = new ConcurrentHashMap<>();

    @Autowired
    ESSearchService esSearchService;

    /**
     * Serialize searchQuery and check any result in cache table . If found returns
     * , if not calls elasticSearch.search and caches it
     * 
     * @param searchQuery
     * @param documentClass
     * @param ttl
     * @return
     * @throws Exception
     */
    public SearchResult search(SearchQuery searchQuery, Class documentClass, long ttl) throws Exception {
        String key = searchQuery.toString();
        if (caches.containsKey(key)) {
            SearchResult searchResult = caches.get(key).getSearchResult();
            searchResult.setResultType(ResultType.SUCCESS_CACHE);
            searchResult.getStatistics().setSearchTime(0);
            if (logger.isDebugEnabled()) {
                logger.debug("[search][CachedResult] key:" + key + " , resultSize:" + searchResult.getResult().size());
            }
            return searchResult;
        }

        SearchResult searchResult = esSearchService.search(searchQuery, documentClass);
        putToCache(searchQuery, searchResult, ttl);
        return searchResult;
    }

    private void putToCache(SearchQuery searchQuery, SearchResult searchResult, long ttl) {
        String key = searchQuery.toString();
        caches.put(key, new CachedSearchResult(searchResult, ttl));
        if (logger.isDebugEnabled()) {
            logger.debug("[putToCache] key:" + key + " , resultSize:" + searchResult.getResult().size());
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void validateCaches() {
        caches.keySet().stream().forEach(key -> {
            CachedSearchResult cachedResult = caches.get(key);
            if (System.currentTimeMillis() > cachedResult.getCreateTime() + cachedResult.getTtl()) {
                caches.remove(key);
                if (logger.isInfoEnabled()) {
                    logger.info("[cacheRemoved] key:" + key + " createdTime : " + cachedResult.getCreateTime()
                            + " , ttl:" + cachedResult.getTtl());
                }
            }
        });
    }
}
