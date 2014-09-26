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
                    },
                    {
                        name: "openesdh/common/widgets/renderers/DateField",
                        config: {
                            currentItem: {"cm:modified": new Date("3214232542432"), "cm:modifier": "admin"},
                            propertyToRender: "cm:modified",
                            renderOnNewLine: true
                        }
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/pages/case/widgets/DashboardService",
            config: {
                nodeRef: page.url.args.nodeRef

            }
        }

    ]
};