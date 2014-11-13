// Site to load data lists from
var siteId = "swsdp";

// Define scopes...
var pubSubScope = "DATA_LIST_SCOPE";

// We need to get the nodeRef for the data list container, the easiest way is just to make
// a REST call to the Repository to get it...
var alfDestination = null;
var result = remote.call("/slingshot/datalists/lists/site/" + siteId + "/dataLists");
if (result.status.code == status.STATUS_OK) {
    alfDestination = JSON.parse(result).container;
}

// Where to pull the available types
var dataListTypesURL = "/api/classes/party_base/subclasses";

// Where to fetch the property metadata.
var allDataListTypesURL = "/api/classes/dl_dataListItem/subclasses";

var newDataListFormControls = [
    {
        name: "alfresco/forms/controls/DojoValidationTextBox",
        config: {
            name: "alf_destination",
            value: alfDestination,
            visibilityConfig: {
                initialValue: false
            }
        }
    },
    {
        name: "alfresco/forms/controls/DojoValidationTextBox",
        config: {
            label: "Title",
            name: "prop_cm_title",
            requirementConfig: {
                initialValue: true
            }
        }
    },
    {
        name: "alfresco/forms/controls/DojoTextarea",
        config: {
            label: "Description",
            name: "prop_cm_description"
        }
    },
    {
        name: "alfresco/forms/controls/DojoSelect",
        config: {
            label: "List Type",
            name: "prop_dl_dataListItemType",
            value: "dl:event",
            optionsConfig: {
                publishTopic: "ALF_GET_FORM_CONTROL_OPTIONS",
                publishPayload: {
                    url: url.context + "/proxy/alfresco" + dataListTypesURL,
                    itemsAttribute: "",
                    labelAttribute: "title",
                    valueAttribute: "name"
                }
            }
        }
    }
];

var newDataListButton = {
    name: "alfresco/buttons/AlfButton",
    config: {
        label: "New List",
        additionalCssClasses: "call-to-action",
        publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
        publishPayloadType: "PROCESS",
        publishPayloadModifiers: ["processCurrentItemTokens"],
        publishPayload: {
            dialogTitle: "New List",
            dialogConfirmationButtonTitle: "Save",
            dialogCancellationButtonTitle: "Cancel",
            formSubmissionTopic: "ALF_CRUD_CREATE",
            formSubmissionPayloadMixin: {
                url: "api/type/dl%3AdataList/formprocessor"
            },
            fixedWidth: true,
            widgets: newDataListFormControls
        }
    }
};

// This function is used to build the views for the main DataList view...
function generateDataListWidgets(views, buttons) {

    // Get all the data list types...
    var dataListTypes = null;
    var result = remote.call(allDataListTypesURL);
    logger.warn(result);
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
                        label: dataListTypes[i].title,
                        value: dataListTypes[i].name
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
                    label: "New " + dataListTypes[i].title + " Item",
                    additionalCssClasses: "call-to-action",
                    publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
                    publishPayloadType: "PROCESS",
                    publishPayloadModifiers: ["processDataBindings"],
                    publishPayload: {
                        dialogTitle: "New " + dataListTypes[i].title + " Item",
                        dialogConfirmationButtonTitle: "Create",
                        dialogCancellationButtonTitle: "Cancel",
                        formSubmissionTopic: "ALF_CRUD_CREATE",
                        formSubmissionPayloadMixin: {
                            url: "api/type/" + dataListTypes[i].name + "/formprocessor",
                            pubSubScope: pubSubScope
                        },
                        fixedWidth: true,
                        widgets: []
                    },
                    publishPayloadSubscriptions: [
                        {
                            topic: pubSubScope + "BLOG_LOAD_DATA_LIST",
                            dataMapping: {
                                nodeRef: "formSubmissionPayloadMixin.alf_destination"
                            }
                        }
                    ],
                    visibilityConfig: {
                        initialValue: false,
                        rules: [
                            {
                                topic: pubSubScope + "BLOG_LOAD_DATA_LIST",
                                attribute: "itemType",
                                is: [dataListTypes[i].name]
                            }
                        ]
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
                            sortable: false
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
                                        propertyToRender: "itemData.prop_" + property.name.replace("party:", "party_") + ".displayValue"
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
                                    pubSubScope: pubSubScope,
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
    name: "openesdh/common/widgets/lists/DataList",
    config: {
        pubSubScope: pubSubScope,
        loadDataPublishTopic: "ALF_CRUD_CREATE",
        itemsProperty: "items",
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

// Define list for selecting the Data List to display...
var dataListList = {
    name: "alfresco/lists/AlfList",
    config: {
        loadDataPublishTopic: "ALF_CRUD_GET_ALL",
        loadDataPublishPayload: {
            url: "slingshot/datalists/lists/site/swsdp/dataLists"
        },
        itemsProperty: "datalists"
    }
};

var dataListListView = [
    {
        name: "alfresco/documentlibrary/views/AlfDocumentListView",
        config: {
            widgets: [
                {
                    id: "VIEW_ROW",
                    name: "alfresco/documentlibrary/views/layouts/Row",
                    config: {
                        widgets: [
                            {
                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                config: {
                                    widgets: [
                                        {
                                            id: "DATA_LIST_TITLE",
                                            name: "alfresco/renderers/InlineEditPropertyLink",
                                            config: {
                                                propertyToRender: "title",
                                                postParam: "prop_cm_title",
                                                refreshCurrentItem: true,
                                                requirementConfig: {
                                                    initialValue: true
                                                },
                                                publishTopic: "ALF_CRUD_CREATE",
                                                publishPayloadType: "PROCESS",
                                                publishPayloadModifiers: ["processCurrentItemTokens", "convertNodeRefToUrl"],
                                                publishPayloadItemMixin: false,
                                                publishPayload: {
                                                    url: "api/node/{nodeRef}/formprocessor",
                                                    noRefresh: true,
                                                    successMessage: "Update success"
                                                },
                                                linkPublishTopic: pubSubScope + "BLOG_LOAD_DATA_LIST",
                                                linkPublishPayloadType: "CURRENT_ITEM"
                                            }
                                        }
                                    ]
                                }
                            },
                            {
                                name: "alfresco/documentlibrary/views/layouts/Cell",
                                config: {
                                    widgets: [
                                        {
                                            name: "alfresco/renderers/PublishAction",
                                            config: {
                                                iconClass: "delete-16",
                                                propertyToRender: "title",
                                                altText: "Delete {0}",
                                                publishTopic: "ALF_CRUD_DELETE",
                                                publishPayloadType: "PROCESS",
                                                publishPayload: {
                                                    requiresConfirmation: true,
                                                    url: "slingshot/datalists/list/node/{nodeRef}",
                                                    confirmationTitle: "Delete Data List",
                                                    confirmationPrompt: "Are you sure you want to delete '{title}'?",
                                                    successMessage: "Successfully deleted '{title}'"
                                                },
                                                publishPayloadModifiers: ["processCurrentItemTokens", "convertNodeRefToUrl"]
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
];
dataListList.config.widgets = dataListListView;


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
                            widthPx: 300,
                            config: {
                                widgets: [
                                    newDataListButton,
                                    dataListList
                                ]
                            }
                        },
                        {
                            name: "alfresco/layout/VerticalWidgets",
                            config: {
                                widgets: [
                                    {
                                        name: "alfresco/layout/HorizontalWidgets",
                                        config: {
                                            widgets: dataListButtons
                                        }
                                    },
                                    dataListView
                                ]
                            }
                        }
                    ]
                }
            }
        ]
    }
}
widgets.push(main);

model.jsonModel = {
    rootNodeId: args.htmlid,

    widgets: widgets,
    services: services
};