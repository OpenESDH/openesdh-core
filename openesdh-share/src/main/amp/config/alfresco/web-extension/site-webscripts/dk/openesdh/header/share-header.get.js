<import resource="classpath:/alfresco/web-extension/utils/case.js">

/**
 * Return an array of create case menu items.
 * @param caseTypes An array of case types to create menu items for
 * @returns {Array}
 */
function getCreateCaseWidgets (caseTypes) {
    var newCaseFolder = getNewCaseFolderNodeRef();
    var widgets = [];
    caseTypes.forEach(function (c) {
        var label = c.label;
        widgets.push(
            {
                name: "alfresco/menus/AlfMenuBarItem",
                config: {
                    id: "CASE_MENU_CREATE_CASE_" + c.type.replace(":", "_").toUpperCase(),
                    label: label,
                    targetUrl: "create-content?destination=" + encodeURIComponent(newCaseFolder) + "&itemId=" + encodeURIComponent(c.type)
                }
            }
        );
    });
    return widgets;
};

var caseTypes = getCaseTypes();
var createCasesWidgets = getCreateCaseWidgets(caseTypes);

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
                                    targetUrl: "hdp/ws/search"
                                }
                            },
                            {
                                name: "alfresco/menus/AlfMenuBarItem",
                                config: {
                                    label: "Saved Searches",
                                    targetUrl: "/"
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
                }
            ]
        }
    });
}