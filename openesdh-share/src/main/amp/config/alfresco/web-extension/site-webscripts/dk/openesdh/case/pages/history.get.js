<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

var caseId = url.templateArgs.caseId;
var caseNodeRef = getCaseNodeRefFromId(caseId);

/*model.jsonModel = {
        services: [
            "alfresco/services/CrudService"
        ],
        widgets: [{

            name: "openesdh/common/widgets/layout/BootstrapContainer",
            config: {
                widgets: [{
                    name: "alfresco/layout/HorizontalWidgets",
                    config: {
                        widgetMarginLeft: 10,
                        widgetMarginRight: 10,
                        widgetWidth: 50,
                        widgets: [{
                            id: "CASE_HISTORY_DASHLET",
                            name: "openesdh/common/widgets/dashlets/CaseHistoryDashlet",
                            config: {
                                nodeRef: caseNodeRef
                            }
                        }]
                     }
                 }]
            }
        }]
};*/

model.jsonModel = {
	    services: [
	        "alfresco/services/CrudService",
	        "openesdh/common/services/PaginationService"
	    ],
	    widgets: [{


	        name: "openesdh/common/widgets/layout/BootstrapContainer",
	        config: {

	            widgets: [{
	                id: "CASE_HISTORY_DASHLET",
	                name: "alfresco/dashlets/Dashlet",
	                config: {
	                    title: msg.get("header.title"),
	                    widgetsForBody: [{
	                            name: "alfresco/lists/AlfSortablePaginatedList",
	                            config: {
	                                loadDataPublishTopic: "GET_HISTORY_ROWS",
	                                loadDataPublishPayload: {
	                                    url: "api/openesdh/casehistory?nodeRef=" + caseNodeRef
	                                },
	                                itemsProperty: "data",
	                                //currentPageSize: 5,
	                                startIndexProperty: "paging.skipCount",
	                                totalResultsProperty: "paging.totalItems",
	                                widgets: [{
	                                    name: "alfresco/lists/views/AlfListView",
	                                    config: {
	                                        additionalCssClasses: "bordered",
	                                        widgetsForHeader: [{
	                                            name: "alfresco/lists/views/layouts/HeaderCell",
	                                            config: {
	                                                label: ""
	                                            }
	                                        }, {
	                                            name: "alfresco/lists/views/layouts/HeaderCell",
	                                            config: {
	                                                label: msg.get("casehistory.column.action"),
	                                            }
	                                        }, {
	                                            name: "alfresco/lists/views/layouts/HeaderCell",
	                                            config: {
	                                                label: msg.get("casehistory.column.type"),
	                                            }
	                                        }, {
	                                            name: "alfresco/lists/views/layouts/HeaderCell",
	                                            config: {
	                                                label: msg.get("casehistory.column.user"),
	                                            }
	                                        }, {
	                                            name: "alfresco/lists/views/layouts/HeaderCell",
	                                            config: {
	                                                label: msg.get("casehistory.column.time"),
	                                            }
	                                        }],
	                                        widgets: [{
	                                            name: "alfresco/lists/views/layouts/Row",
	                                            config: {
	                                                widgets: [{
	                                                    name: "alfresco/lists/views/layouts/Cell",
	                                                    config: {
	                                                        widgets: [{
	                                                            config: {
	                                                                propertyToRender: "type"
	                                                            }
	                                                        }]
	                                                    }
	                                                }, {
	                                                    name: "alfresco/lists/views/layouts/Cell",
	                                                    config: {
	                                                        additionalCssClasses: "mediumpad",
	                                                        widgets: [{
	                                                            name: "alfresco/renderers/Property",
	                                                            config: {
	                                                                propertyToRender: "action"
	                                                            }
	                                                        }]
	                                                    }
	                                                }, {
	                                                    name: "alfresco/lists/views/layouts/Cell",
	                                                    config: {
	                                                        additionalCssClasses: "mediumpad",
	                                                        widgets: [{
	                                                            name: "alfresco/renderers/Property",
	                                                            config: {
	                                                                propertyToRender: "type"
	                                                            }
	                                                        }]
	                                                    }
	                                                }, {
	                                                    name: "alfresco/lists/views/layouts/Cell",
	                                                    config: {
	                                                        additionalCssClasses: "mediumpad",
	                                                        widgets: [{
	                                                            name: "alfresco/renderers/Property",
	                                                            config: {
	                                                                propertyToRender: "user"
	                                                            }
	                                                        }]
	                                                    }
	                                                }, {
	                                                    name: "alfresco/lists/views/layouts/Cell",
	                                                    config: {
	                                                        additionalCssClasses: "mediumpad",
	                                                        width: "200px",
	                                                        widgets: [{
	                                                            name: "alfresco/renderers/Date",
	                                                            config: {
	                                                                simple: true,
	                                                                propertyToRender: "time"
	                                                            }
	                                                        }]
	                                                    }
	                                                }]
	                                            }
	                                        }]
	                                    }
	                                }]
	                            }
	                        }, {
	                            name: "alfresco/layout/LeftAndRight",
	                            config: {
	                                widgets: [{
	                                        name: "openesdh/common/widgets/lists/CustomPaginator",
	                                        //name: "alfresco/lists/Paginator",
	                                        align: "right"
	                                    }

	                                ]
	                            }

	                        }

	                    ]
	                }
	            }]
	        }

	    }]
	};
