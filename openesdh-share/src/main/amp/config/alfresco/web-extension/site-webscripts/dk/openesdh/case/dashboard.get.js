var caseNodeRef = url.args.nodeRef;
var caseId = url.templateArgs.caseId;

model.jsonModel = {
    services: [ "alfresco/services/CrudService", "openesdh/common/services/CaseMembersService" ],
    widgets: [{
            name: "alfresco/layout/HorizontalWidgets",
            config: {
                widgetMarginLeft: 10,
                widgetMarginRight: 10,
                widgetWidth: 50,
                widgets: [
                    {
                        name: "alfresco/layout/VerticalWidgets",
                        config:{
                            widgets:[
                                {
                                    id: "CASE_INFO_DASHLET",
                                    name: "openesdh/common/widgets/dashlets/CaseInfoDashlet"
                                },
                                {
                                    id: "CONTACTS_DASHLET",
                                    name: "openesdh/common/widgets/dashlets/MyCasesWidget"
                                }
                            ]
                        }
                    } ,
                    {
                        name: "alfresco/layout/VerticalWidgets",
                        config:{
                            widgets:[
                                {
                                    name: "openesdh/common/widgets/dashlets/CaseHistoryDashlet"
                                },
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
                                }
                            ]
                        }
                    }
                ]
            }
    }]

};