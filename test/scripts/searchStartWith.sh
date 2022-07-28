. ./base.sh
debug_log "Search Start with $1 = $2 and limit $3"

data=$(curl -s -X GET "${P_ELASTIC_BASE}/${P_INDEX_NAME}/_search" -H 'Content-Type: application/json' -d'
{
  "query": {
    "prefix": {
       "'$1'":{
          "value": "'$2'"
       } 
     }
  }
}')


if [[ ! -z "$3" ]];then
   echo $data | jq ".hits.hits[]._source$3"
else
  echo $data  | jq
fi
