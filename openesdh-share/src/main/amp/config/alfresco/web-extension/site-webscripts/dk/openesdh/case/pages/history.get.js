<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

var caseId = url.templateArgs.caseId;
var caseNodeRef = getCaseNodeRefFromId(caseId);

model.jsonModel = {
        services: [
            "alfresco/services/CrudService"
        ],
        widgets: [{

            name: "alfresco/layout/BootstrapContainer",
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
};