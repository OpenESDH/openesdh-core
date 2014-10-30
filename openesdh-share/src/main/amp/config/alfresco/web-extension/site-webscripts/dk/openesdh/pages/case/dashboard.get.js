model.jsonModel = {
    widgets: [
        {
            name: "alfresco/layout/HorizontalWidgets",
            config: {
                widgetMarginLeft: 10,
                widgetMarginRight: 10,
                widgetWidth: 50,
                widgets: [
                    {
                        name: "openesdh/common/widgets/dashlets/CaseInfoDashlet"
                    },
                    {
                        name: "openesdh/common/widgets/dashlets/CaseInfoDashlet"
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/pages/case/services/Dashboard",
            config: {
                caseNodeRef: page.url.args.nodeRef
            }
        }

    ]
};