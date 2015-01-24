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

function getFormDefinition() {

    var formWidgets = [
        {
            name: "alfresco/layout/HorizontalWidgets",
            id: "FIRST_MIDDLE_NAME_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "Firstname",
                            name: "firstname",
                            label: "First Name",
                            description: "Your firstname",
                            placeHolder: "Firstname",
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
                            name: "middlename",
                            label: "Middle Name",
                            description: "Your middlename",
                            placeHolder: "Middle Name",
                            requirementConfig: { initialValue: false}
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/layout/HorizontalWidgets",
            id: "LAST_NAME_CPR_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "Lastname",
                            name: "lastname",
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
                            fieldId: "CPRNumber",
                            name: "CPR-Number",
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
            name: "alfresco/layout/HorizontalWidgets",
            id: "EMAIL_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "Email",
                            name: "e-mail",
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
                            visibilityConfig: userCanCreateAccounts
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/layout/HorizontalWidgets",
            id: "EMAIL_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "Password",
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
                            fieldId: "Password_Validate",
                            name: "validate_password",
                            label: "Validate Password",
                            description: "retype your password to validate",
                            placeHolder: "",
                            visibilityConfig: userAccountWidgets
                        }
                    }
                ]
            }
        },
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
            name: "alfresco/layout/HorizontalWidgets",
            id: "HOUSE_NUMBER_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "HouseNumber",
                            width:"4",
                            maxLength: 3,
                            name: "houseNumber",
                            label: "House Number",
                            description: "House number of the address",
                            placeHolder: "42",
                            validationConfig: [{
                                    validation: "regex",
                                    regex: "^\\d{2,3}$"
                            }],
                            requirementConfig: { initialValue: false}

                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "StreetName",
                            name: "street",
                            label: "Street",
                            description: "The name of the street",
                            placeHolder: "Street Name",
                            requirementConfig: { initialValue: false}
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/layout/HorizontalWidgets",
            id: "FLOOR_PBOX_SUITE_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "Floor",
                            name: "floorNumber",
                            label: "Floor",
                            width:"4",
                            maxLength: 3,
                            description: "The floor on which the address of the user is located within the building, if any",
                            placeHolder: "01",
                            requirementConfig: { initialValue: false},
                            validationConfig: [{
                                    validation: "regex",
                                    regex: "^(\\d){2,3}$",
                                    errorMessage: ""
                                }
                            ]
                        }
                    },
                    {
                        name: "alfresco/forms/controls/DojoValidationTextBox",
                        config: {
                            fieldId: "Suite",
                            name: "suite",
                            label: "Suite",
                            description: "The suite if applicable",
                            placeHolder: "Suite"
                        }
                    },
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "PostBox",
                            name: "postBox",
                            label: "Post Box",
                            width:"7",
                            maxLength: 6,
                            description: "Post box if applicable",
                            placeHolder: "P.O Box",
                            requirementConfig: { initialValue: false}
                        }
                    }
                ]
            }
        },
        {
            name: "alfresco/layout/HorizontalWidgets",
            id: "POSTCODE_CITY_ROW",
            config: {
                widgetMarginLeft: 5,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/common/widgets/controls/form/VariableWidthDojoValidationTextBox",
                        config: {
                            fieldId: "PostCode",
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
                            fieldId: "City",
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
                            fieldId: "CountryCode",
                            name: "countryCode",
                            label: "Country Code",
                            width:"4",
                            maxLength: 2,
                            description: "The two digit country code e.g. DK",
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
    ];
    return formWidgets;
}

function generateContactPageWidgets() {

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
                                                label: msg.get("contacts.tool.heading.contact.person"),
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
                                    widgets: [
                                        {
                                            name: "alfresco/buttons/AlfButton",
                                            config: {
                                                label: msg.get("contacts.tool.create.contact.person"),
                                                additionalCssClasses: "call-to-action",
                                                publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
                                                publishPayloadType: "PROCESS",
                                                publishPayloadModifiers: ["processCurrentItemTokens"],
                                                publishPayload: {
                                                    dialogTitle: "contacts.tool.create-dialog.label",
                                                    dialogConfirmationButtonTitle: msg.get("create.button.label"),
                                                    dialogCancellationButtonTitle: msg.get("cancel.button.label"),
                                                    formSubmissionTopic: "ALF_CRUD_CREATE",
                                                    fixedWidth: true,
                                                    widgets: getFormDefinition(true)
                                                }
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
}
