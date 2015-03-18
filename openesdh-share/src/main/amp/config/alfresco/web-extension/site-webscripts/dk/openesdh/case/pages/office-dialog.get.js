model.jsonModel = {
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