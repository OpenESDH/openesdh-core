//var title = widgetUtils.findObject(model.jsonModel, "id", "HEADER_TITLE");
//title.config.label = "header.title";

var args = page.url.args;

var navMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");



navMenu.config.widgets.push({
    id: "HEADER_CASE_DOCUMENTS",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_DOCUMENTS",
        label: "header.case.documents" ,
        title: "header.case.documents.altText",
        targetUrl: "hdp/ws/dk-openesdh-pages-documents?nodeRef=" + args.nodeRef
    }
});


navMenu.config.widgets.push({
    id: "HEADER_CASE_MEMBERS",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_MEMBERS",
        iconClass: "alf-user-icon",
        iconAltText: "header.case.members.altText",
//        title: "header.case.members.title",
        targetUrl: "hdp/ws/dk-openesdh-pages-case-dashboard-members?nodeRef=" + args.nodeRef
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
        iconAltText: "header.case.config.altText",
  //      title: "header.case.config.title", does not work :-(
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

        publishTopic: "JOURNALIZE",
        publishPayload: {}
    }
});

navMenu.config.widgets.push(caseConfig);