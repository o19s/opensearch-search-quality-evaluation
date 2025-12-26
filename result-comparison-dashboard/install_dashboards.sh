
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

echo "${MAJOR}Deleting index result_comparison${RESET}"
curl -s -X DELETE $opensearch/result_comparison/

echo "${MAJOR}Creating index result_comparison${RESET}"
curl -s -X PUT $opensearch/result_comparison/
     
echo "${MAJOR}Populating index result_comparison with sample metric data${RESET}"
curl -s -H 'Content-Type: application/x-ndjson' -XPOST "$opensearch/result_comparison/_bulk?pretty=false&filter_path=-items" --data-binary @sample_data.ndjson 

echo "${MAJOR}\nInstalling Search Result Comparison Dashboards${RESET}"
curl -X POST "$opensearch_dashboard/api/saved_objects/_import?overwrite=true" -H "osd-xsrf: true" --form file=@result_comparison_dashboard.ndjson > /dev/null

echo "${MAJOR}The Search Result Comparison Dashboards were successfully installed${RESET}"
