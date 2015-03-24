<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">
<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/oe.js">

//try {null.ex;} catch(e) {logger.log("\n\n(global-customisations/share/header/share-header.get.js) Linenumber: " + e.lineNumber)+"\n\n";}

var caseTypes = getCaseTypes();

function getStatusLabels() {
    var optionStatus = [];
    var states = getCaseStatusTypes();

    for (var state in states) {
        optionStatus.push({
            value: msg.get("create-case.status.constraint.value." + states[state]),
            label: msg.get("create-case.status.constraint.label." + states[state])
        });
    }
    return optionStatus;
}

function getCreateCaseWidgets(){
    var userNodeRef = getUserNodeRef(user.name);
    var caseContainerNodeRef = getNewCaseFolderNodeRef();
        return [
            {
                name: "alfresco/forms/ControlRow",
                config: {
                    description: "",
                    title: "",
                    fieldId: "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                    widgets: [
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                fieldId: "edb22ed0-ch9a-48f4-8f30-c5atjd748ffb",
                                name: "alf_destination",
                                value: caseContainerNodeRef,
                                label: "",
                                unitsLabel: "",
                                description: "",
                                postWhenHiddenOrDisabled: true,
                                noValueUpdateWhenHiddenOrDisabled: false,
                                validationConfig: {
                                    regex: ".*"
                                },
                                placeHolder: "",
                                widgets: []
                            },
                            widthPc: "1"
                        },
                        {
                            name: "alfresco/forms/controls/HiddenValue",
                            config: {
                                fieldId: "edb31ed0-c74a-48f4-8f30-c5atbd748ffb",
                                value: "",
                                label: "",
                                unitsLabel: "",
                                description: "",
                                postWhenHiddenOrDisabled: true,
                                noValueUpdateWhenHiddenOrDisabled: false,
                                validationConfig: {
                                    regex: ".*"
                                },
                                placeHolder: "",
                                widgets: []
                            },
                            widthPc: "1"
                        }
                    ]
                }
            },
            {
                name: "alfresco/forms/ControlRow",
                config: {
                    description: "",
                    title: "",
                    fieldId: "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                    widgets: [
                        {
                            name: "alfresco/forms/controls/DojoValidationTextBox",
                            config: {
                                fieldId: "edb19ed0-c74a-48f4-8f30-c5aabd74fffb",
                                name: "prop_cm_title",
                                value: "",
                                label: msg.get("create-case.label.title"),
                                unitsLabel: "",
                                description: "",
                                visibilityConfig: {
                                    initialValue: true,
                                    rules: []
                                },
                                requirementConfig: {
                                    initialValue: false,
                                    rules: []
                                },
                                disablementConfig: {
                                    initialValue: false,
                                    rules: []
                                },
                                postWhenHiddenOrDisabled: true,
                                noValueUpdateWhenHiddenOrDisabled: false,
                                validationConfig: {
                                    regex: ".*"
                                },
                                placeHolder: "",
                                widgets: []
                            },
                            widthPc: "50"
                        },
                        {
                            name: "alfresco/forms/controls/DojoSelect",
                            config: {
                                id: "prop_oe_status",
                                label: msg.get("create-case.label.button.case-status"),
                                optionsConfig: {
                                    fixed: getStatusLabels()
                                },
                                unitsLabel: "",
                                description: "",
                                name: "prop_oe_status",
                                fieldId: "ebf9b987-9744-47ad-8823-ee9d9aa783f4",
                                widgets: []
                            }
                        }
                    ]
                }
            },
            {
                name: "alfresco/forms/ControlRow",
                config: {
                    description: "",
                    title: "",
                    fieldId: "88ba8d88-b562-4954-81b9-d34ac564d5ff",
                    widgets: [
                        {
                            id: "create_case_owner_widget",
                            name: "openesdh/common/widgets/controls/WrappedYUIAuthorityWidget",
                            config: {
                                value: userNodeRef,
                                name: "assoc_case_owners_added",
                                visibilityConfig: { initialValue: true },
                                fieldId: "b03a9abe-c4be-44aa-8e1f-c1048172eba1",
                                multiple: true,
                                label: msg.get("create-case.label.button.case-owner")
                            }
                        }
                    ]
                }
            },
            {
                name: "alfresco/forms/ControlRow",
                config: {
                    description: "",
                    title: "",
                    fieldId: "b0632dac-002e-4860-884b-b9237246075c",
                    widgets: [
                        {
                            name: "openesdh/common/widgets/controls/DojoDateExt",
                            config: {
                                id: "prop_case_startDate",
                                unitsLabel: "mm/dd/yy",
                                description: "",
                                label: msg.get("create-case.label.button.start-date"),
                                name: "prop_case_startDate",
                                fieldId: "b4bd606f-66ae-4f06-847d-dfdc77f5abc2",
                                widgets: []
                            }
                        },
                        {
                            name: "openesdh/common/widgets/controls/DojoDateExt",
                            config: {
                                id: "prop_case_endDate",
                                unitsLabel: "mm/dd/yy",
                                description: "",
                                label: msg.get("create-case.label.button.end-date"),
                                name: "prop_case_endDate",
                                fieldId: "69707d94-0f8c-4966-832a-a1adbc53b74f",
                                widgets: []
                            }
                        }
                    ]
                }
            },
            {
                        name: "alfresco/forms/ControlRow",
                        config: {
                            description: "",
                            title: "",
                            fieldId: "0b4ab71a-26ce-4df9-839f-c26b12fffecb",
                            widgets: [
                                {
                                    name: "alfresco/forms/controls/DojoTextarea",
                                    config: {
                                        fieldId: "63854d9e-295a-454d-8c0d-685de6f68d71",
                                        name: "prop_cm_description",
                                        value: "",
                                        label: "Description",
                                        unitsLabel: "",
                                        description: "",
                                        visibilityConfig: {
                                            initialValue: true,
                                            rules: []
                                        },
                                        requirementConfig: {
                                            initialValue: false,
                                            rules: []
                                        },
                                        disablementConfig: {
                                            initialValue: false,
                                            rules: []
                                        },
                                        postWhenHiddenOrDisabled: true,
                                        noValueUpdateWhenHiddenOrDisabled: false,
                                        widgets: []
                                    },
                                    widthPc: "98"
                                }
                            ]
                        }
                    }
        ]
}

/**
 * Return an array of create case menu items.
 * @param caseTypes An array of case types to create menu items for
 * @returns {Array}
 */
function getCreateCaseMenuWidgets (caseTypes) {
    var widgets = [];
    caseTypes.forEach(function (c) {
        var label = c.label;
        widgets.push(
            {
                name: "alfresco/menus/AlfMenuBarItem",
                config: {
                    publishTopic: "ALF_CREATE_FORM_DIALOG_REQUEST",
                    id: "CASE_MENU_CREATE_CASE_" + c.type.replace(":", "_").toUpperCase(),
                    label: label,
                    publishPayloadType: "PROCESS",
                    publishPayloadModifiers: ["processCurrentItemTokens"],
                    publishPayload: {
                        dialogTitle: "contacts.tool.create-dialog.label",
                        dialogConfirmationButtonTitle: msg.get("create.button.label"),
                        dialogCancellationButtonTitle: msg.get("cancel.button.label"),
                        formSubmissionTopic: "OE_CREATE_CASE_TOPIC",
                        formSubmissionPayloadMixin: {
                            successResponseTopic: "OE_CREATE_CASE_SUCCESS"
                        },
                        fixedWidth: true,
                        widgets: getCreateCaseWidgets()
                    }
                }
            }
        );
    });
    return widgets;
}

var createCasesWidgets = getCreateCaseMenuWidgets(caseTypes);

var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
if (headerMenu != null) {
    headerMenu.config.widgets.push({
        id: "HEADER_CASES_DROPDOWN",
        name: "alfresco/header/AlfMenuBarPopup",
        config: {
            label: "Cases",
            widgets: [
                {
                    name: "alfresco/menus/AlfMenuGroup",
                    config: {
                        label: "Search",
                        widgets: [
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    id: "CASE_MENU_SEARCH_LINK",
                                    label: "Search",
                                    targetUrl: "oe/case/search"
                                }
                            },
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    label: "Saved Searches",
                                    disabled: true
                                }
                            }
                        ]
                    }
                },
                {
                    name: "alfresco/menus/AlfMenuGroup",
                    config: {
                        id: "CASE_MENU_CREATE_CASE_GROUP",
                        label: "Create",
                        widgets: createCasesWidgets
                    }
                },
                // This widget is required to handle ALF_UPLOAD_REQUEST topics
                {
                    name: "alfresco/upload/AlfUpload"
                }
            ]
        }
    });

    headerMenu.config.widgets.push(
        {
            name: "alfresco/menus/AlfMenuBarItem",
            config: {
                label: "Dagsorden",
                disabled: true
            }
        }
    );
}
model.jsonModel.services.push("openesdh/common/services/CaseService");
model.jsonModel.services.push("alfresco/dialogs/AlfDialogService");
