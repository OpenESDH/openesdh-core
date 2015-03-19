model.jsonModel = {
    widgets: [
        {
            name: "alfresco/layout/ClassicWindow",
            config: {
                title: "Office Dialog",
                widgets: [
                    {
                        name: "alfresco/layout/VerticalWidgets",
                        config: {
                            fieldId: "2f83a87e-536d-4f04-83e8-07b0d4a353c3",
                            widgets: [
                                {
                                    id: "officeForm",
                                    name: "alfresco/forms/Form",
                                    config: {
                                        okButtonPublishTopic: "OFFICE_INTEGRATION_OK",
                                        okButtonPublishGlobal: true,
                                        cancelButtonPublishTopic: "OFFICE_INTEGRATION_CANCEL",
                                        cancelButtonPublishGlobal: true,
                                        widgets: [
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
            name: "openesdh/common/services/OutlookCaseService",
            config: {
                formId: "outlookForm"
            }
        },
        "alfresco/dialogs/AlfDialogService",
        "alfresco/services/DocumentService"
    ]
};