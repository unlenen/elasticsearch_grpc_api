. ./base.sh
debug_log "Delete Index"
curl -s -XDELETE ${P_ELASTIC_BASE}/${P_INDEX_NAME}|jq
