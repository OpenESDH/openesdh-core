<import resource="classpath:/alfresco/web-extension/utils/parties.js">//</import>

var partiesFolderNodeRef = getPartiesFolderNodeRef();

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
                    publishTopic: "LEGACY_CREATE_FORM_DIALOG",
                    publishPayloadType: "PROCESS",
                    publishPayloadModifiers: ["processDataBindings"],
                    publishPayload: {
                        nodeRef: partiesFolderNodeRef,
                        itemType: "party:person",
                        dialogTitle: "New Person",
                        successResponseTopic: "PARTY_LIST_SHOW_ALL"
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
                                iconClass: "edit-16",
                                publishTopic: "LEGACY_EDIT_FORM_DIALOG",
                                publishPayloadType: "PROCESS",
                                publishPayload: {
                                    dialogTitle: 'Edit Person "{cm_name}"',
                                    nodeRef: "{nodeRef}",
                                    successResponseTopic: "PARTY_LIST_RELOAD"
                                },
                                publishGlobal: true,
                                publishPayloadModifiers: ["processCurrentItemTokens"]
                            }
                        },
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
                                    requiresConfirmation: true,
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
    "alfresco/services/NotificationService",
    "openesdh/common/services/LegacyFormService"
);

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
                            id: "PARTIES_PAGINATION_MENU",
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
