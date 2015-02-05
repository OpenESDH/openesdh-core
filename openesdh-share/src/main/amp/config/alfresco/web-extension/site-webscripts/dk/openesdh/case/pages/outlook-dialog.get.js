model.jsonModel = {
    widgets: [
        {
            id: "outlookForm",
            name: "alfresco/forms/Form",
            config: {
                widgets: [
                    {
                        name: "openesdh/common/widgets/controls/CasePicker",
                        config: {
                            name: "case",
                            label: msg.get("page.case.label"),
                            itemKey: "nodeRef"
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            name: "title",
                            label: msg.get("page.title.label")
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            name: "responsible",
                            label: msg.get("page.responsible.label")
                        }
                    },
                    {
                        name: "aoi/common/widgets/controls/SelectionList",
                        config: {
                            name: "attachments",
                            label: msg.get("page.attachments.label"),
                            initiallySelected: true,
                            itemKey: "name",
                            widgets: [
                                {
                                    name: "alfresco/lists/AlfList",
                                    config: {
                                        waitForPageWidgets: true,
                                        loadDataPublishTopic: null,
                                        itemKey: "name",
                                        noDataMessage: msg.get("page.no-attachments.message"),
                                        widgets: [
                                            {
                                                name: "alfresco/documentlibrary/views/AlfDocumentListView",
                                                config: {
                                                    itemKey: "name",
                                                    widgets: [
                                                        {
                                                            name: "alfresco/documentlibrary/views/layouts/Row",
                                                            config: {
                                                                widgets: [
                                                                    {
                                                                        name: "alfresco/documentlibrary/views/layouts/Cell",
                                                                        config: {
                                                                            additionalCssClasses: "mediumpad",
                                                                            widgets: [
                                                                                {
                                                                                    name: "aoi/common/widgets/BetterSelector",
                                                                                    config: {
                                                                                        itemKey: "name"
                                                                                    }
                                                                                }
                                                                            ]
                                                                        }
                                                                    },
                                                                    {
                                                                        name: "alfresco/documentlibrary/views/layouts/Cell",
                                                                        config: {
                                                                            additionalCssClasses: "mediumpad",
                                                                            widgets: [
                                                                                {
                                                                                    name: "alfresco/renderers/Property",
                                                                                    config: {
                                                                                        propertyToRender: "name",
                                                                                        renderAsLink: false
                                                                                    }
                                                                                }
                                                                            ]
                                                                        }
                                                                    }
                                                                ]
                                                            }
                                                        }
                                                    ]
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        }
    ],
    services: [
        {
            name: "aoi/common/services/OfficeIntegrationService",
            config: {
                formId: "outlookForm"
            }
        }
    ]
};