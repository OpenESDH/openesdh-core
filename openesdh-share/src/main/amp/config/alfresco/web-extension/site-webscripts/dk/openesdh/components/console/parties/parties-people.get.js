// This function is used to build the views for the main DataList view...
function generateDataListWidgets(views, buttons) {
    // Get all the data list types...
    var dataListTypes = null;
    var result = remote.call("/api/classes/party_person/subclasses");
    if (result.status.code == status.STATUS_OK) {
        // Iterate over each data list type and get it's properties...
        dataListTypes = JSON.parse(result);
        for (var i = 0; i < dataListTypes.length; i++) {
            var widgetsForHeader = [];
            var rowWidgets = [];

            var currentView = {
                name: "alfresco/documentlibrary/views/AlfDocumentListView",
                config: {
                    viewSelectionConfig: {
                        label: "table",
                        value: "table"
                    },
                    additionalCssClasses: "bordered",
                    widgetsForHeader: widgetsForHeader,
                    widgets: [
                        {
                            name: "alfresco/documentlibrary/views/layouts/Row",
                            config: {
                                widgets: rowWidgets
                            }
                        }
                    ]
                }
            };
            views.push(currentView);


            var newItemButton = {
                name: "alfresco/buttons/AlfDynamicPayloadButton",
                config: {
                    label: "New Person",
                    additionalCssClasses: "call-to-action",
                    publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
                    publishPayloadType: "PROCESS",
                    publishPayloadModifiers: ["processDataBindings"],
                    publishPayload: {
                        dialogTitle: "New Person",
                        dialogConfirmationButtonTitle: "Create",
                        dialogCancellationButtonTitle: "Cancel",
                        formSubmissionTopic: "ALF_CRUD_CREATE",
                        formSubmissionPayloadMixin: {
                            url: "api/type/" + dataListTypes[i].name.replace(":", "_") + "/formprocessor"
                        },
                        fixedWidth: true,
                        widgets: []
                    }
                }
            };
            buttons.push(newItemButton);

            // Iterate over the view properties...
            properties = dataListTypes[i].properties;
            for (var key in properties) {
                if (key.indexOf("party:") === 0) {
                    // Only add DataList namespaced properties...
                    var property = properties[key];

                    widgetsForHeader.push({
                        name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                        config: {
                            label: property.title || property.name,
                            sortable: true
                        }
                    });

                    rowWidgets.push({
                        name: "alfresco/documentlibrary/views/layouts/Cell",
                        config: {
                            additionalCssClasses: "siteName mediumpad",
                            widgets: [
                                {
                                    name: "alfresco/renderers/Property",
                                    config: {
                                        propertyToRender: property.name
                                    }
                                }
                            ]
                        }
                    });

                    newItemButton.config.publishPayload.widgets.push({
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            name: "prop_" + property.name.replace("party:", "party_"),
                            label: property.title || property.name,
                            description: property.description,
                            value: property.defaultValue,
                            requirementConfig: {
                                initialValue: property.mandatory
                            }
                        }
                    });
                }
            }

            widgetsForHeader.push({
                name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                config: {
                    label: "Actions",
                    sortable: false
                }
            });

            rowWidgets.push({
                name: "alfresco/documentlibrary/views/layouts/Cell",
                config: {
                    additionalCssClasses: "mediumpad",
                    widgets: [
                        {
                            name: "alfresco/renderers/PublishAction",
                            config: {
                                iconClass: "delete-16",
                                publishTopic: "ALF_CRUD_CREATE",
                                publishPayloadType: "PROCESS",
                                publishPayload: {
                                    // TODO:
                                    url: "slingshot/datalists/action/items?alf_method=delete",
                                    nodeRefs: ["{nodeRef}"],
                                    confirmationTitle: "Delete Data List Item",
                                    confirmationPrompt: "Are you sure you want to delete the item'?",
                                    successMessage: "Successfully deleted"
                                },
                                publishGlobal: true,
                                publishPayloadModifiers: ["processCurrentItemTokens"]
                            }
                        }
                    ]
                }
            });
        }
    }
};

// Generate the Data List specific widgets...
var dataListButtons = [];
var dataListViews = [];
generateDataListWidgets(dataListViews, dataListButtons);

var dataListView = {
    name: "openesdh/common/widgets/lists/PartyList",
    config: {
        partyType: "party:person",

        loadDataPublishTopic: "ALF_CRUD_GET_ALL",
        itemsProperty: "",
        widgets: dataListViews
    }
};

var services = [],
    widgets = [];

// Append required services...
services.push("alfresco/services/CrudService",
    "alfresco/services/OptionsService",
    "alfresco/dialogs/AlfDialogService",
    "alfresco/services/NotificationService");

var main = {
    name: "alfresco/layout/VerticalWidgets",
    config: {
        baseClass: "side-margins",
        widgets: [
            {
                name: "alfresco/html/Spacer",
                config: {
                    height: "14px"
                }
            },
            {
                name: "alfresco/layout/HorizontalWidgets",
                config: {
                    widgets: [
                        {
                            name: "alfresco/layout/VerticalWidgets",
                            config: {
                                widgets: [
                                    {
                                        name: "alfresco/html/Heading",
                                        config: {
                                            label: "People Search",
                                            level: 2
                                        }
                                    },
                                    {
                                        name: "alfresco/html/Spacer",
                                        config: {
                                            height: "8px"
                                        }
                                    }
                                ]
                            }
                        },
                        {
                            name: "alfresco/layout/HorizontalWidgets",
                            config: {
                                widgets: dataListButtons
                            }
                        }
                    ]
                }
            },
            {
                name: "alfresco/forms/SingleTextFieldForm",
                config: {
                    useHash: false,
                    showOkButton: true,
                    okButtonLabel: "Search",
                    showCancelButton: false,
                    okButtonPublishTopic: "PARTY_LIST_SEARCH",
                    okButtonPublishGlobal: true,
                    textBoxLabel: "Search",
                    textFieldName: "term",
                    okButtonIconClass: "alf-white-search-icon",
                    okButtonClass: "call-to-action",
                    textBoxIconClass: "alf-search-icon",
                    textBoxCssClasses: "long"
                }
            },
            dataListView,
            {
                name: "alfresco/layout/CenteredWidgets",
                config: {
                    widgets: [
                        {
                            id: "DOCLIB_PAGINATION_MENU",
                            name: "alfresco/documentlibrary/AlfDocumentListPaginator",
                            widthCalc: 430
                        }
                    ]
                }
            }
        ]
    }
};

widgets.push(main);

model.jsonModel = {
    rootNodeId: args.htmlid,

    widgets: widgets,
    services: services
};
