var title = widgetUtils.findObject(model.jsonModel, "id", "HEADER_TITLE");
title.config.label = "header.title";


var args = page.url.args;

var navMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");

navMenu.config.widgets.push({
    id: "HEADER_CASE_MEMBERS",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_MEMBERS",
        iconClass: "alf-user-icon",
        iconAltText: "header.menu.members.altText",
        title: "header.menu.members.altText",
        targetUrl: "/members"
    }
});


// Create the basic site configuration menu...
var caseConfig = {
    id: "HEADER_CASE_CONFIGURATION_DROPDOWN",
    name: "alfresco/menus/AlfMenuBarPopup",
    config: {
        id: "HEADER_CASE_CONFIGURATION_DROPDOWN",
        label: "",
        iconClass: "alf-configure-icon",
        iconAltText: "header.menu.caseConfig.altText",
        title: "header.menu.caseConfig.altText",
        widgets: []
    }
};

caseConfig.config.widgets.push({
    id: "HEADER_CASE_EDIT",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_EDIT",
        label: "header.case.edit" ,
        iconClass: "alf-cog-icon",
        targetUrl: "edit-metadata?nodeRef=" + args.nodeRef
    }
});

caseConfig.config.widgets.push({
    id: "HEADER_CASE_JOURNALIZE",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_JOURNALIZE",
        label: "header.case.journalize" ,
        iconClass: "alf-cog-icon",
        targetUrl: "/dashboard"
    }
});

navMenu.config.widgets.push(caseConfig);