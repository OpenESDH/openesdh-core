
var searchResultsWidgetContainer = widgetUtils.findObject(model.jsonModel.widgets, "id", "FCTSRCH_SEARCH_ADVICE_NO_RESULTS");
if(searchResultsWidgetContainer){
    searchResultsWidgetContainer.config.widgets[0] = {
        id: "FCTSRCH_SEARCH_RESULT",
        name: "openesdh/search/AlfSearchResult",
        config: {
            enableContextMenu: false
        }
    }
}
