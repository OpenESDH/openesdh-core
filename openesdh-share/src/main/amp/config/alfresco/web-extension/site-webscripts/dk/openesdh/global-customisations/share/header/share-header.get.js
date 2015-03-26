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
                        dialogId: "CREATE_CASE_DIALOG",
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
model.jsonModel.services.push("openesdh/common/services/AuthorityService");
model.jsonModel.services.push("alfresco/services/DialogService");