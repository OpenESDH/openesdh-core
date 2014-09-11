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
                        name: "openesdh-case/widgets/InfoWidget"
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
    ]
};