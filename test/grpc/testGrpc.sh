key="$1"
operator="$2"
value="$3"

./grpcurl -plaintext  -d '{"ttl":10000,"index":"test_machines","conditions":{"conditionType":"AND","statement":[{"key":"'${key}'","operator":"'${operator}'","value":"'${value}'"}]}}' localhost:9898 es_search.SearchService.search
