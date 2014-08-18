var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
if (headerMenu != null) {
    headerMenu.config.widgets.push({
        id: "HEADER_CUSTOM_DROPDOWN",
        name: "alfresco/header/AlfMenuBarPopup",
        config: {
            label: "Cases",
            widgets: [
                {
                    name: "alfresco/menus/AlfMenuGroup",
                    config: {
                        label: "Search",
                        widgets: [
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    label: "Search",
                                    targetUrl: "/"
                                }
                            },
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    label: "Saved Searches",
                                    targetUrl: "/"
                                }
                            }
                        ]
                    }
                },
                {
                    name: "alfresco/menus/AlfMenuGroup",
                    config: {
                        label: "Create",
                        widgets: [
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    label: "Simple Case",
                                    targetUrl: "/"
                                }
                            },
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    label: "Project Other",
                                    targetUrl: "/"
                                }
                            }
                        ]
                    }
                }
            ]
        }
    });
}