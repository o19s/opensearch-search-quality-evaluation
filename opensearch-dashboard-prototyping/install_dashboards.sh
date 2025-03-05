
# Ansi color code variables
ERROR='\033[0;31m'
MAJOR='\033[0;34m'
MINOR='\033[0;37m    '
RESET='\033[0m' # No Color

opensearch=$1
opensearch_dashboard=$2
set -eo pipefail

if [ -z "$opensearch" ]; then
    echo "Error: please pass in both the opensearch url and the opensearch dashboard url"
    exit 1
fi
if [ -z "$opensearch_dashboard" ]; then
    echo "Error: please pass in both the opensearch url and the opensearch dashboard url"
    exit 1
fi

echo "${MAJOR}Using Open Search and Open Search Dashboards at $opensearch and $opensearch_dashboard respectively.${RESET}"

echo "${MAJOR}Deleting index srw_metrics_sample_data${RESET}"
curl -s -X DELETE $opensearch/srw_metrics_sample_data/

echo "${MAJOR}Creating index srw_metrics_sample_data with mappings${RESET}"
curl -s -X PUT $opensearch/srw_metrics_sample_data/ -H "Content-Type: application/json" -d @srw_metrics_mappings.json

     
echo "${MAJOR}Populating index srw_metrics_sample_data with sample metric data${RESET}"
curl -s -H 'Content-Type: application/x-ndjson' -XPOST "$opensearch/srw_metrics_sample_data/_bulk?pretty=false&filter_path=-items" --data-binary @sample_data.ndjson 

echo "${MAJOR}\nInstalling Quality Evaluation Framework Dashboards${RESET}"
curl -X POST "$opensearch_dashboard/api/saved_objects/_import?overwrite=true" -H "osd-xsrf: true" --form file=@search_dashboard.ndjson > /dev/null

echo "${MAJOR}The Quality Evaluation Framework Dashboards were successfully installed${RESET}"
