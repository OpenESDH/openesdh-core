define(["dojo/_base/declare"],
    function(declare) {

    return declare(null, {
        CaseSearchTopic: "CASE_SEARCH_TOPIC",
        CaseFiltersApplyTopic: "CASE_FILTERS_APPLY_TOPIC",
        CaseFilterRemoveTopic: "CASE_FILTER_REMOVE_TOPIC",      
        CaseSortColumnTopic: "CASE_SORT_COLUMN_TOPIC",
        CaseListColumnsChanged: "CASE_LIST_COLUMNS_CHANGED",
        CaseListChangePage: "CASE_LIST_CHANGE_PAGE",
        CaseListPageSizeChanged: "CASE_LIST_PAGE_SIZE_CHANGED",
        
        CaseManageSavedSearchesTopic: "CASE_MANAGE_SAVED_SEARCHES_TOPIC",
        
        CaseFiltersSetTopic: "CASE_FILTERS_SET_TOPIC",
        CaseFiltersSetAckTopic: "CASE_FILTERS_SET_ACK_TOPIC",
        CaseSearchSetTopic: "CASE_SEARCH_SET_TOPIC"
   });
});