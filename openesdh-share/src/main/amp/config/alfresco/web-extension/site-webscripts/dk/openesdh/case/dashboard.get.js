<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

var caseId = url.templateArgs.caseId;
var caseNodeRef = getCaseNodeRefFromId(caseId);

model.jsonModel = {
    services: [
        "alfresco/services/CrudService",
        "openesdh/common/services/CaseMembersService",
        "alfresco/dialogs/AlfDialogService"
    ],
    widgets: [{
            name: "alfresco/layout/HorizontalWidgets",
            config: {
                widgetMarginLeft: 10,
                widgetMarginRight: 10,
                widgetWidth: 50,
                widgets: [
                    { name: "openesdh/common/widgets/dashlets/CaseInfoDashlet" },
                    { id: "CASE_MEMBERS_DASHLET",
                      name: "alfresco/layout/HorizontalWidgets",
                      align: "right",
                      config: {
                            widgetWidth: 50,
                            widgets: [{
                                name: "openesdh/common/widgets/dashlets/CaseMembersDashlet",
                                config:{
                                    caseId : caseId
                                }
                            }]
                      }
                    },
                    {

                        name: "openesdh/common/widgets/dashlets/CaseHistoryDashlet",
                        config: {
                            caseId: caseId
                        }
                    },

                    {
                        name: "openesdh/common/widgets/dashlets/NotesDashlet",
                        config: {
                            nodeRef: caseNodeRef

                        }
                    }
                ]
            }
    }]

};