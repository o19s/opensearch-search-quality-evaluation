
# Ansi color code variables
ERROR='\033[0;31m'
MAJOR='\033[0;34m'
MINOR='\033[0;37m    '
RESET='\033[0m' # No Color

: ${OPEN_SEARCH:="localhost:9200"}
: ${OPEN_SEARCH_DASHBOARDS:="localhost:5601"}

set -eo pipefail

echo "${MAJOR}Using Open Search and Open Search Dashboards at $OPEN_SEARCH and $OPEN_SEARCH_DASHBOARDS respectively.${RESET}"
echo "  (set environment variables OPEN_SEARCH and OPEN_SEARCH_DASHBOARDS otherwise)\n"

echo "${MAJOR}Deleting index sqe_metrics_sample_data${RESET}"
curl -s -X DELETE http://$OPEN_SEARCH/sqe_metrics_sample_data/ > /dev/null

echo "${MAJOR}Populating index sqe_metrics_sample_data with sample metric data${RESET}"
curl -s -H 'Content-Type: application/x-ndjson' -XPOST "$OPEN_SEARCH/sqe_metrics_sample_data/_bulk?pretty=false&filter_path=-items" --data-binary @sample_data.ndjson > /dev/null

echo "${MAJOR}\nInstalling Quality Evaluation Framework Dashboards${RESET}"
curl -X POST "http://$OPEN_SEARCH_DASHBOARDS/api/saved_objects/_import?overwrite=true" -H "osd-xsrf: true" --form file=@search_dashboard.ndjson > /dev/null

echo "${MAJOR}The Quality Evaluation Framework Dashboards were successfully installed${RESET}"