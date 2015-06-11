<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">

var caseId = url.templateArgs.caseId;
var caseNodeRef = getCaseNodeRefFromId(caseId);
var isReadOnly = !hasWritePermission(caseId);

var caseWorkflowService= {
    name: "openesdh/common/services/CaseWorkflowService",
    config:{
        caseId: caseId,
        nodeRef: (caseNodeRef != null) ? caseNodeRef : args.destination
    }
};
model.jsonModel = {
    services: [
        "alfresco/services/CrudService",
        "openesdh/common/services/CaseMembersService"
    ],
    widgets: [{
            name: "alfresco/layout/HorizontalWidgets",
            config: {
                widgetMarginLeft: 10,
                widgetMarginRight: 10,
                widgetWidth: 50,
                widgets: [
                    {
                        name: "alfresco/layout/VerticalWidgets",
                        config: {
                            widgets: [
                                {
                                    id: "CASE_INFO_DASHLET",
                                    name: "openesdh/common/widgets/dashlets/CaseInfoDashlet"
                                },
                                {
                                    id: "CASE_MEMBERS_DASHLET",
                                    name: "openesdh/common/widgets/dashlets/CaseMembersDashlet",
                                    config:{
                                        caseId : caseId
                                    }
                                },
                                {
                                    id: "CASE_NOTES_DASHLET",
                                    name: "openesdh/common/widgets/dashlets/NotesDashlet",
                                    config: {
                                        nodeRef: caseNodeRef,
                                        isReadOnly: isReadOnly
                                    }
                                }
                            ]
                        }
                    },
                    {
                        name: "alfresco/layout/VerticalWidgets",
                        config: {
                            widgets: [
                                {
                                    id: "CASE_HISTORY_DASHLET",
                                    name: "openesdh/common/widgets/dashlets/CaseHistoryDashlet",
                                    config: {
                                        nodeRef: caseNodeRef
                                    }
                                },
                                {
                                    id: "CASE_WORKFLOW_DASHLET",
                                    name: "openesdh/common/widgets/dashlets/CaseWorkflowsDashlet",
                                    config: {
                                        caseId : caseId,
                                        caseNodeRef: caseNodeRef,
                                        isReadOnly: isReadOnly
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
    }]

};

model.jsonModel.services.push(caseWorkflowService);