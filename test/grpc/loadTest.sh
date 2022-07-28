key="region"
operator="EQUAL"
value="earth"

begin=$(date +%s)

P_STEP=$1
if [[ -z "$P_STEP" ]];then
  P_STEP=100
fi

P_INDEX=0;
while [[ 1 = 1 ]];do 
	let "P_INDEX=P_INDEX+1"
        
	P_PERCENT=$(( 100 * $P_INDEX / $P_STEP))

	./grpcurl -plaintext -d '{"ttl":10000,"index":"test_machines","conditions":{"conditionType":"AND","statement":[{"key":"'${key}'","operator":"'${operator}'","value":"'${value}'"}]}}' localhost:9898 es_search.SearchService.search > /dev/null

	if [[ $P_INDEX -gt $P_STEP ]];then
		break
	fi

	printf "\r Load Test %s${P_PERCENT} , Step:${P_INDEX}/${P_STEP}" '%'
done

printf "\r\n"
end=$(date +%s)
passed_time=$(( end - begin))
echo "Load test completed in $passed_time ms"
