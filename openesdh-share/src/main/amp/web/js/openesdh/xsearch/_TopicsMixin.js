define(["dojo/_base/declare"],
    function(declare) {

    return declare(null, {
        SearchTopic: "XSEARCH_SEARCH_TOPIC",
        FiltersApplyTopic: "XSEARCH_FILTERS_APPLY_TOPIC",
        FilterRemoveTopic: "XSEARCH_FILTER_REMOVE_TOPIC",
        SortColumnTopic: "XSEARCH_SORT_COLUMN_TOPIC",
        ListColumnsChanged: "XSEARCH_LIST_COLUMNS_CHANGED",
        ListChangePage: "XSEARCH_LIST_CHANGE_PAGE",
        ListPageSizeChanged: "XSEARCH_LIST_PAGE_SIZE_CHANGED",
        
        ManageSavedSearchesTopic: "XSEARCH_MANAGE_SAVED_SEARCHES_TOPIC",
        
        FiltersSetTopic: "XSEARCH_FILTERS_SET_TOPIC",
        FiltersSetAckTopic: "XSEARCH_FILTERS_SET_ACK_TOPIC",
        SearchSetTopic: "XSEARCH_SEARCH_SET_TOPIC"
   });
});