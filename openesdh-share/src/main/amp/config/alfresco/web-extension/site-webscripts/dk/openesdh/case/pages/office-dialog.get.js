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
                                                    label: msg.get("officedialog.caseid.label"),
                                                    name: "caseId",
                                                    assignTo: "pickedCaseWidget"
                                                }
                                            },
                                            {
                                                name: "alfresco/buttons/AlfButton",
                                                config: {
                                                    label: msg.get("officedialog.findcase.label"),
                                                    publishTopic: "OE_FIND_CASE",
                                                    publishGlobal: true,
                                                    assignTo: "formDialogButton"
                                                }
                                            },
                                            {
                                                name: "alfresco/buttons/AlfButton",
                                                config: {
                                                    label: msg.get("officedialog.createcase.label"),
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
                                                    label: msg.get("officedialog.title.label"),
                                                    additionalCssClasses: "openesdh-office-title-field",
                                                    width: "50em"
                                                }
                                            },
                                            {
                                                "name": "alfresco/html/Spacer",
                                                config: {
                                                    height: "20px",
//                                                    additionalCssClasses: "top-border-beyond-gutters",
//                                                    visibilityConfig: isMainDocumentView
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