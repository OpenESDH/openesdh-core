model.jsonModel = {
    widgets: [
        {
            name: "alfresco/layout/HorizontalWidgets",
            config: {
                widgetWidth: 50,
                widgets: [
                    {
                        name: "openesdh/common/widgets/dashlets/CaseInfoDashlet",
                        config: {
                            title: "Hej"
                        }
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/pages/case/widgets/InfoWidgetService",
            config: {
                nodeRef: page.url.args.nodeRef

            }
        }

    ]
};