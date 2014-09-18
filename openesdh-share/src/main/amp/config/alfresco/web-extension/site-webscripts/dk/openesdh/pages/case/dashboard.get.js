model.jsonModel = {
    widgets: [
        {
            id: "SET_PAGE_TITLE",
            name: "alfresco/header/SetTitle",
            config: {
                title: msg.get("header.title")
            }
        },
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
                        /*
                        ,
                        widgets: [
                            {
                                name: "openesdh/pages/case/widgets/InfoWidget",
                                config: {}
                            }
                        ]
                        */
                    },
                    {
                        name: "alfresco/buttons/AlfButton",
                        config: {
                            label: "Hello world!"
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