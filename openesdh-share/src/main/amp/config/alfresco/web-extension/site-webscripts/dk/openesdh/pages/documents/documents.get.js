model.jsonModel = {
    widgets: [
        {
            name: "alfresco/layout/VerticalWidgets",
            config: {
                widgetMarginLeft: 10,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/pages/documents/widgets/DocumentList",
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
            name: "openesdh/pages/documents/services/Documents",
            config: {
                caseNodeRef: page.url.args.nodeRef

            }
        }

    ]
};