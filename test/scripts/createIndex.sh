. ./base.sh
debug_log "Create Index"
curl -s -XPUT ${P_ELASTIC_BASE}/${P_INDEX_NAME}|jq
