export P_ELASTIC_BASE="http://localhost:9200"
export P_INDEX_NAME="test_machines"
export P_TEST_DOC="../data/MOCK_DATA.json"
export P_INDEX_MAPPING_FILE="../mappings/test_machines.json"

function debug_log(){
  echo "[DEBUG] $1 : url:$P_ELASTIC_BASE , index:$P_INDEX_NAME"
}
