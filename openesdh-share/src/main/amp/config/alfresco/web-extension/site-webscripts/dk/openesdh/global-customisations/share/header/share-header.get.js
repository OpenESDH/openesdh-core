<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/case.js">
<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/oe.js">

var args = page.url.args;
var caseId = url.templateArgs.caseId;
//In the case of some pages (e.g. document details) we need to get the caseId in reverse
// i.e. from the nodeRef
if (caseId == null)
    caseId = getCaseIdFromNodeRef(args.nodeRef);

var nodeRef = getCaseNodeRefFromId(caseId);

//try {null.ex;} catch(e) {logger.log("\n\n(global-customisations/share/header/share-header.get.js) Linenumber: " + e.lineNumber)+"\n\n";}

var caseTypes = getCaseTypes();

//This object is initialised when the above variable is also initialised in the getCaseTypes() function.
var createFormsArray = {};

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
                    publishTopic: "OE_SHOW_CREATE_CASE_DIALOG",
                    publishPayload: {caseType : c.type.substr(0, c.type.indexOf(":")) },
                    id: "CASE_MENU_CREATE_CASE_" + c.type.replace(":", "_").toUpperCase(),
                    label: label
                }
            }
        );
        createFormsArray[c.typeName] = c.createForm;
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
            label: msg.get("header.cases.menu.label"),
            widgets: [
                {
                    name: "alfresco/menus/AlfMenuGroup",
                    config: {
                        label: msg.get("header.cases.menu.search.group"),
                        widgets: [
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    id: "CASE_MENU_SEARCH_LINK",
                                    label: msg.get("header.cases.menu.search"),
                                    targetUrl: "oe/case/search"
                                }
                            },
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    label: msg.get("header.cases.menu.savedsearch"),
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
                        label: msg.get("header.cases.menu.create.group"),
                        widgets: createCasesWidgets
                    }
                },
                // This widget is required to handle ALF_UPLOAD_REQUEST topics
                {
                    name: "openesdh/common/services/UploadService"
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

var caseService= {
    name: "openesdh/common/services/CaseService",
    config:{
        caseId: caseId,
        casesFolderNodeRef: getNewCaseFolderNodeRef(),
        createCaseWidgets: createFormsArray,
        nodeRef: (nodeRef != null) ? nodeRef : args.destination,
        currentUser: getCurrentUser(),
        caseConstraintsList: getCaseConstraints()
    }
};
model.jsonModel.services.push(caseService);
model.jsonModel.services.push("openesdh/common/services/AuthorityService");
model.jsonModel.services.push("alfresco/services/OptionsService");
model.jsonModel.services.push("alfresco/services/DialogService");