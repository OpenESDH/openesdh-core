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
                                            {
                                                name: "alfresco/forms/controls/DojoValidationTextBox",
                                                config: {
                                                    name: "caseId",
                                                    assignTo: "pickedCaseWidget",
                                                    label: "Case",
                                                    additionalCssClasses: "openesdh-office-title-field"
                                                }
                                            },
                                            {
                                                name: "alfresco/buttons/AlfButton",
                                                config: {
                                                    label: "Find Case",
                                                    publishTopic: "OE_FIND_CASE",
                                                    publishGlobal: true
                                                },
                                                assignTo: "formDialogButton",
                                            },
                                            {
                                                name: "alfresco/buttons/AlfButton",
                                                config: {
                                                    label: "Create Case",
                                                    publishTopic: "OE_CREATE_CASE",
                                                    publishGlobal: true
                                                }
                                            },
                                            {
                                                "name": "alfresco/html/Spacer",
                                                config: {
                                                    height: "14px",
//                                                    additionalCssClasses: "top-border-beyond-gutters",
//                                                    visibilityConfig: isMainDocumentView
                                                }
                                            },
                                            {
                                                name: "alfresco/forms/controls/DojoValidationTextBox",
                                                config: {
                                                    name: "title",
                                                    label: "Title",
                                                    cssRequirements: [{cssFile: "MyStyle.css"}]
                                                }
                                            }
//                                            {
//                                                name: "openesdh/xsearch/Grid",
//                                                config: {
//                                                    baseType: "case:base",
//                                                    rowsPerPage: 2,
//                                                    pageSizeOptions: []
//                                                }
//                                            }
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
            name: "openesdh/common/services/OfficeIntegrationService",
            config: {
                formId: "officeForm"
            }
        },
        "alfresco/dialogs/AlfDialogService",
        "alfresco/services/DocumentService"
    ]
};