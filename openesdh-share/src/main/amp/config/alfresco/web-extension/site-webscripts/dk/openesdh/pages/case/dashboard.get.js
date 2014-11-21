var caseNodeRef = url.args.nodeRef;

model.jsonModel = {
    services: [ "alfresco/services/CrudService" ],
    widgets: [{
            name: "alfresco/layout/LeftAndRight",
            config: {
                widgets: [
                    {
                        id: "CASE_INFO_DASHLET",
                        name: "alfresco/layout/HorizontalWidgets",
                        align: "left",
                        config: {
                            widgetWidth: 50,
                            widgets: [{
                                name: "openesdh/common/widgets/dashlets/CaseInfoDashlet"
                            }]
                        }
                        },{
                        id: "CASE_MEMBERS_DASHLET",
                        name: "alfresco/layout/HorizontalWidgets",
                        align: "right",
                        config: {
                            widgetWidth: 50,
                            widgets: [
                                {
                                    name: "openesdh/common/widgets/dashlets/CaseMembersDashlet" ,
                                    config:{caseNodeRef: caseNodeRef }
                                }
                            ]
                        }
                    }
                ]
            }
    }]
};