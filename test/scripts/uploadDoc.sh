. base.sh

P_LINE_SIZE=$(cat $P_TEST_DOC|jq -c ".[] | objects"| wc -l)
P_INDEX=0
P_SUCCESS=0

debug_log "Upload Documents , Doc Size: $P_LINE_SIZE"

for line in $(cat $P_TEST_DOC |jq -c ".[] | objects");do

  let "P_INDEX=P_INDEX+1"
  P_PERCENT=$(( 100 * $P_INDEX / $P_LINE_SIZE))

  is_success=$(curl -s -X POST "$P_ELASTIC_BASE/$P_INDEX_NAME/_doc/$P_INDEX" \
	  -H "Content-Type: application/json" \
	  -d "$line"|jq "._shards.successful")
  if [[ "$is_success" -eq 1 ]];then
    let "P_SUCCESS=P_SUCCESS+1"
  fi

  printf "\rUploaded %s of document(%s) . Success Count %d/%d " "%$P_PERCENT" "$P_TEST_DOC" "$P_SUCCESS" "$P_LINE_SIZE"
done

echo " "
