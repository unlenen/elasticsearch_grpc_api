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
package unlenen.es.search.fe.service;

import org.springframework.stereotype.Service;

import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import unlenen.es.search.fe.exception.SearchException;
import unlenen.es.search.proto.SearchQuery;
import unlenen.es.search.proto.SearchResult;
import unlenen.es.search.proto.SearchServiceGrpc;

/**
 *
 * @author Nebi Volkan UNLENEN(unlenen@gmail.com)
 */
@Service
public class SearchService {

    @GrpcClient("unlenenEsSearchBe")
    private SearchServiceGrpc.SearchServiceBlockingStub grpcService;

    public SearchResult search(SearchQuery request) throws SearchException {
        try {
            return this.grpcService.search(request);
        } catch (StatusRuntimeException e) {
            throw new SearchException(e.getMessage(), e);
        }
    }
}
