var userCanCreate = "CREATION_ALLOWED_TOPIC";

function checkUserCanCreateAccount(){
    return {
        publishTopic: userCanCreate,
        publishPayloadType: "CONFIGURED",
        publishPayload: {
            creationAllowed: user.isAdmin
        }
    }
}

var userCanCreateAccounts = {
    initialValue: true,
    rules: [
        {
            topic: userCanCreate,
            attribute: "creationAllowed",
            is: [true],
            isNot: [false]
        }
    ]
};

var userAccountWidgets = {
    initialValue: false,
    rules: [
        {
            targetId: "USER_ACCOUNT_CREATION",
            is: [true]
        }
    ]
};

function getAddressWidgets(){
    return [
        {
            name: "alfresco/html/Spacer",
            config: {
                height: "14px"
            }
        },
        {
            "name": "alfresco/html/Heading",
            config: {
                level: "2",
                label: "Address"
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            id: "HOUSE_NUMBER_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "streetName",
                            name: "streetName",
                            label: "Street",
                            description: "The name of the street",
                            placeHolder: "Street Name",
                            requirementConfig: { initialValue: false}
                        }
                    },
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "houseNumber",
                            width:"4",
                            maxLength: 3,
                            name: "houseNumber",
                            label: "House Number",
                            description: "House number of the address",
                            placeHolder: "42",
                            requirementConfig: { initialValue: false}

                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            id: "FLOOR_PBOX_SUITE_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "floorNumber",
                            name: "floorNumber",
                            label: "Floor",
                            width:"4",
                            maxLength: 3,
                            description: "The floor on which the address of the user is located within the building, if any",
                            placeHolder: "01",
                            requirementConfig: { initialValue: false}
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "suite",
                            name: "suite",
                            label: "Suite",
                            description: "The suite if applicable",
                            placeHolder: "Suite"
                        }
                    },
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "postBox",
                            name: "postBox",
                            label: "Post Box",
                            width:"7",
                            maxLength: 4,
                            description: "Post box if applicable",
                            placeHolder: "P.O Box",
                            requirementConfig: { initialValue: false}
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            id: "POSTCODE_CITY_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "postCode",
                            name: "postCode",
                            label: "Post Code",
                            width:"4",
                            maxLength: 4,
                            description: "The post code of the address",
                            placeHolder: "8000",
                            requirementConfig: { initialValue: false},
                            validationConfig: [
                                {
                                    validation: "regex",
                                    regex: "^(\\d){4}$",
                                    errorMessage: ""
                                }
                            ]
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "city",
                            name: "city",
                            label: "City",
                            description: "The city",
                            placeHolder: "Århus",
                            requirementConfig: { initialValue: false}
                        }
                    },
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "countryCode",
                            name: "countryCode",
                            label: "Country Code",
                            width:"4",
                            maxLength: 2,
                            description: "The two digit country code e.g. DK",
                            value: "DK",
                            placeHolder: "DK",
                            requirementConfig: { initialValue: false},
                            validationConfig: [
                                {
                                    validation: "regex",
                                    regex: "^[a-zA-Z]{2}$",
                                    errorMessage: ""
                                }
                            ]
                        }
                    }
                ]
            }
        }
    ]
}

function getFormDefinition(contactType) {

    var formWidgets = [
        {
            name: "alfresco/forms/ControlRow",
            id: "FIRST_MIDDLE_NAME_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "firstName",
                            name: "firstName",
                            label: "First Name",
                            description: "Your first name",
                            placeHolder: "First name",
                            requirementConfig: {
                                initialValue: true,
                                errorMessage: "Required"
                            }
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "MiddleName",
                            name: "middleName",
                            label: "Middle Name",
                            description: "Your middle name",
                            placeHolder: "Middle Name",
                            requirementConfig: { initialValue: false}
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            id: "LAST_NAME_CPR_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "lastName",
                            name: "lastName",
                            label: "Last Name",
                            description: "Sur/Family name",
                            placeHolder: "Surname/Family Name",
                            requirementConfig: {
                                initialValue: true,
                                errorMessage: "Required"
                            }
                        }
                    },
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "cprNumber",
                            name: "cprNumber",
                            label: "CPR Number",
                            width: "7",
                            maxLength: 10,
                            description: "12345678",
                            placeHolder: "12345678",
                            requirementConfig: { initialValue: false},
                            validationConfig: [{
                                validation: "regex",
                                regex: "^\\d{10}$"
                            }]
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            id: "EMAIL_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "email",
                            name: "email",
                            label: "E-mail",
                            description: "User email",
                            placeHolder: "user_email@example.com",
                            requirementConfig: { initialValue: true},
                            validationConfig: [{
                                validation: "regex",
                                regex: "^[a-zA-Z0-9\\._%+-]+@[a-zA-Z0-9\\.-]+\\.[a-zA-Z]{2,4}$",
                                errorMessage: "Valid E-mail Address Required"
                            }]
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoCheckBox",
                        config: {
                            fieldId: "USER_ACCOUNT_CREATION",
                            name: "createUserAccount",
                            label: "Create User Account",
                            description: "Tick this field to create a corresponding user account",
                            value: false,
                            visibilityConfig: {initialValue: false}
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/forms/ControlRow",
            id: "PASSWORD_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "password",
                            name: "password",
                            label: "Password",
                            description: "Psssword",
                            placeHolder: "",
                            visibilityConfig: userAccountWidgets
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "validate_password",
                            name: "validate_password",
                            label: "Validate Password",
                            description: "retype your password to validate",
                            placeHolder: "",
                            visibilityConfig: userAccountWidgets
                        }
                    }
                ]
            }
        }
    ];

    if(contactType != "PERSON"){

        formWidgets = "";

        formWidgets =[
            {
                name: "alfresco/forms/ControlRow",
                id: "NAME_ROW",
                config: {
                    widgetMarginLeft: 5,
                    widgetMarginRight: 10,
                    widgets: [
                        {
                            name: "alfresco/forms/controls/DojoValidationTextBox",
                            config: {
                                fieldId: "organizationName",
                                name: "organizationName",
                                label: "Company Name",
                                description: "The company's name",
                                placeHolder: "Magenta Aps",
                                requirementConfig: {
                                    initialValue: true,
                                    errorMessage: "Required"
                                }
                            }
                        }
                    ]
                }
            },
            {
                name: "alfresco/forms/ControlRow",
                id: "CVR_ROW",
                config: {
                    widgetMarginLeft: 5,
                    widgetMarginRight: 10,
                    widgets: [
                        {
                            name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                            config: {
                                fieldId: "cvrNumber",
                                name: "cvrNumber",
                                label: "CVR Number",
                                width: "7",
                                maxLength: 8,
                                description: "12345678",
                                placeHolder: "12345678",
                                requirementConfig: { initialValue: false},
                                validationConfig: [{
                                    validation: "regex",
                                    regex: "^\\d{8}$"
                                }]
                            }
                        }
                    ]
                }
            },
            {
                name: "alfresco/forms/ControlRow",
                id: "EMAIL_ROW",
                config: {
                    widgetMarginLeft: 5,
                    widgetMarginRight: 10,
                    widgets: [
                        {
                            name: "alfresco/forms/controls/DojoValidationTextBox",
                            config: {
                                fieldId: "email",
                                name: "email",
                                label: "E-mail",
                                description: "User email",
                                placeHolder: "firma@example.com",
                                requirementConfig: { initialValue: true},
                                validationConfig: [{
                                    validation: "regex",
                                    regex: "^[a-zA-Z0-9\\._%+-]+@[a-zA-Z0-9\\.-]+\\.[a-zA-Z]{2,4}$",
                                    errorMessage: "Valid E-mail Address Required"
                                }]
                            }
                        }
                    ]
                }
            }
        ]
    }

    return formWidgets.concat(getAddressWidgets());;
}

function getSelectedItemActions(contactType) {
    var actionsMenu = {
        id: "CONTACTS_SELECTED_ITEMS_MENU",
        name: "alfresco/documentlibrary/AlfSelectedItemsMenuBarPopup",
        config: {
            label: msg.get("contacts.tool.actions"),
            widgets: [
                {
                    id: "DOCLIB_SELECTED_ITEMS_MENU_GROUP1",
                    name: "alfresco/menus/AlfMenuGroup",

                    config: {
                        widgets: getMultiSelectActions(contactType)
                    }
                }
            ]
        }
    };
    return actionsMenu;
}

function getMultiSelectActions(contactType) {
    var contactTypeId = contactType.replace(":", "_");
    var actionSet = [];

    var action = {
        name: "alfresco/menus/AlfMenuItem",
        config: {
            iconImage: url.context + "/res/components/images/delete-16.png",
            label: msg.get("contacts.tool.delete-multiple." + contactTypeId + ".action"),
            publishTopic: "CONTACTS_DELETE_MULTIPLE",
            publishPayload: {
                contactType: contactType
            },
            publishGlobal: true
        }
    };
    actionSet.push(action);
    return actionSet;
}

function generateContactTableView(contactType) {
    var contactTypeId = contactType.replace(":", "_");
    return {
        name: "openesdh/common/widgets/lists/views/DynamicColumnsTableView",
        config: {
            itemType: contactType,
            columnsReadyTopic: "CONTACT_LIST_SHOW_ALL",
            additionalCssClasses: "bordered",

            widgets: [
                {
                    name: "alfresco/documentlibrary/views/layouts/Row",
                    config: {}
                }
            ],
            extraWidgetsForHeaderBefore: [
                {
                    name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                    config: {
                        label: '',
                        sortable: false
                    }
                }
            ],
            extraWidgetsForHeader: [
                {
                    name: "alfresco/documentlibrary/views/layouts/HeaderCell",
                    config: {
                        label: msg.get("contacts.tool.actions"),
                        sortable: false
                    }
                }
            ],
            extraRowWidgetsBefore: [
                {
                    name: "alfresco/documentlibrary/views/layouts/Cell",
                    config: {
                        additionalCssClasses: "mediumpad",
                        widgets: [
                            {
                                name: "alfresco/renderers/Selector"
                            }
                        ]
                    }
                }
            ],
            extraRowWidgets: [
                {
                    name: "alfresco/documentlibrary/views/layouts/Cell",
                    config: {
                        additionalCssClasses: "mediumpad",
                        widgets: [
                            {
                                name: "openesdh/common/widgets/renderers/PublishAction",
                                config: {
                                    iconClass: "edit-16",
                                    altText: msg.get("contacts.tool.edit." + contactTypeId + ".action"),
                                    publishTopic: "LEGACY_EDIT_FORM_DIALOG",
                                    publishPayloadType: "PROCESS",
                                    publishPayload: {
                                        dialogTitle: msg.get("contacts.tool.edit." + contactTypeId),
                                        nodeRef: "{nodeRef}",
                                        successResponseTopic: "CONTACT_LIST_RELOAD"
                                    },
                                    publishGlobal: true,
                                    publishPayloadModifiers: ["processCurrentItemTokens"]
                                }
                            },
                            {
                                name: "openesdh/common/widgets/renderers/PublishAction",
                                config: {
                                    iconClass: "delete-16",
                                    altText: msg.get("contacts.tool.delete." + contactTypeId + ".action"),
                                    publishTopic: "ALF_CRUD_DELETE",
                                    publishPayloadType: "PROCESS",
                                    publishPayload: {
                                        // TODO: We are abusing this URL a bit.
                                        // It is intended for deleting data lists, but we are deleting individual nodes
                                        url: "slingshot/datalists/list/node/{nodeRef}",
                                        confirmationTitle: msg.get("contacts.tool.delete." + contactTypeId + ".confirmation.title"),
                                        confirmationPrompt: msg.get("contacts.tool.delete." + contactTypeId + ".confirmation.message"),
                                        requiresConfirmation: true,
                                        successMessage: msg.get("contacts.tool.delete." + contactTypeId + ".success")
                                    },
                                    publishGlobal: true,
                                    publishPayloadModifiers: ["processCurrentItemTokens", "convertNodeRefToUrl"]
                                }
                            }
                        ]
                    }
                }
            ]
        }
    };
}

function generateContactPageWidgets(contactType, cType) {
    var contactTypeId = contactType.replace(":", "_");

    var contactListViews = [generateContactTableView(contactType)];

    // TODO: Add browse hierarchy view for Organizations contacts

    var contactList = {
        name: "openesdh/common/widgets/lists/ContactList",
        config: {
            contactType: contactType,
            loadDataPublishTopic: "ALF_CRUD_GET_ALL",
            itemsProperty: "items",
            widgets: contactListViews
        }
    };

    return {
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
                    name: "alfresco/html/Heading",
                    config: {
                        label: msg.get("contacts.tool.heading." + contactTypeId),
                        level: 2
                    }
                },
                {
                    name: "alfresco/layout/VerticalWidgets",
                    config: {
                        widgets: [
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
                        widgets: [
                            {
                                name: "openesdh/common/widgets/forms/SingleTextFieldForm",
                                widthPc : 85,
                                config: {
                                    id:"CONTACT"+cType.toUpperCase()+"_SEARCH_FORM",
                                    useHash: false,
                                    showOkButton: true,
                                    okButtonLabel: msg.get("contacts.tool.search.button"),
                                    showCancelButton: false,
                                    okButtonPublishTopic: "CONTACT_LIST_SEARCH",
                                    okButtonPublishGlobal: true,
                                    textBoxLabel: msg.get("contacts.tool.search.button"),
                                    textFieldName: "searchTerm",
                                    okButtonIconClass: "alf-white-search-icon",
                                    okButtonClass: "call-to-action contact-search-ok-button",
                                    textBoxIconClass: "alf-search-icon",
                                    textBoxCssClasses: "long",
                                    textBoxRequirementConfig: {
                                        initialValue: false
                                    }
                                }
                            },
                            {
                                name: "alfresco/buttons/AlfButton",
                                widthPc : 10,
                                config: {
                                    id:"CREATE_"+cType.toUpperCase()+"_BTN",
                                    label: msg.get("contacts.tool.create.contact_"+cType.toLowerCase()),
                                    additionalCssClasses: "call-to-action",
                                    publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
                                    publishPayloadType: "PROCESS",
                                    publishPayloadModifiers: ["processCurrentItemTokens"],
                                    publishPayload: {
                                        dialogId:"CREATE_"+cType.toUpperCase()+"_DIALOG",
                                        dialogTitle: "contacts.tool.create-dialog.label",
                                        dialogConfirmationButtonTitle: msg.get("create.button.label"),
                                        dialogCancellationButtonTitle: msg.get("cancel.button.label"),
                                        formSubmissionTopic: "ALF_CRUD_CREATE",
                                        formSubmissionPayloadMixin: {
                                            url: "/api/openesdh/contacts/create",
                                            contactType: cType,
                                            successResponseTopic: "CONTACT_LIST_SHOW_ALL"
                                        },
                                        fixedWidth: true,
                                        widgets: getFormDefinition(cType)
                                    }
                                }
                            }
                        ]
                    }
                },
                {
                    id: "CONTACT_LIST_TOOLBAR",
                    name: "alfresco/menus/AlfMenuBar",
                    align: "left",
                    config: {
                        widgets: [
                            {
                                name: "alfresco/documentlibrary/AlfSelectDocumentListItems",
                                config: {
                                    widgets: [
                                        {
                                            name: "alfresco/menus/AlfMenuGroup",
                                            config: {
                                                widgets: [
                                                    {
                                                        name: "alfresco/menus/AlfMenuItem",
                                                        config: {
                                                            label: "All",
                                                            publishTopic: "ALF_DOCLIST_FILE_SELECTION",
                                                            publishPayload: {
                                                                label: "select.all.label",
                                                                value: "selectAll"
                                                            }
                                                        }
                                                    },
                                                    {
                                                        name: "alfresco/menus/AlfMenuItem",
                                                        config: {
                                                            label: "None",
                                                            publishTopic: "ALF_DOCLIST_FILE_SELECTION",
                                                            publishPayload: {
                                                                label: "select.none.label",
                                                                value: "selectNone"
                                                            }
                                                        }
                                                    },
                                                    {
                                                        name: "alfresco/menus/AlfMenuItem",
                                                        config: {
                                                            label: "Invert",
                                                            publishTopic: "ALF_DOCLIST_FILE_SELECTION",
                                                            publishPayload: {
                                                                label: "invert.selection.label",
                                                                value: "selectInvert"
                                                            }
                                                        }
                                                    }
                                                ]
                                            }
                                        }
                                    ]
                                }
                            },
                            getSelectedItemActions(contactType)
                        ]
                    }
                },
                contactList,
                {
                    name: "alfresco/layout/CenteredWidgets",
                    config: {
                        widgets: [
                            {
                                id: "CONTACTS_PAGINATION_MENU",
                                name: "alfresco/documentlibrary/AlfDocumentListPaginator",
                                widthCalc: 430
                            }
                        ]
                    }
                }
            ]
        }
    }
}
