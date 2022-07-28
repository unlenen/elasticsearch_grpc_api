. ./base.sh
debug_log "Upload Mappings"
curl -s -X PUT ${P_ELASTIC_BASE}/${P_INDEX_NAME}/_mapping \
	-H "Content-Type: application/json" \
       	-d "@../mappings/test_machines.json" |jq
