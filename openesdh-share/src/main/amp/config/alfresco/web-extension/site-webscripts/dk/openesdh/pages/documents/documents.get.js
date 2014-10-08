model.jsonModel = {
    widgets: [
        {
            name: "alfresco/layout/VerticalWidgets",
            config: {
                widgetMarginLeft: 10,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/pages/documents/widgets/Documents",
                        config: {
                            title: "Hej"
                        }
                    },
                    {
                        name: "openesdh/pages/documents/widgets/Documents",
                        config: {
                            title: "Hej 2"
                        }
                    }

                ]
            }
        }
    ],
    services: [
        {
            name: "openesdh/pages/documents/services/Documents",
            config: {
                nodeRef: page.url.args.nodeRef

            }
        }

    ]
};