<import resource="classpath:/alfresco/web-extension/utils/oe.js">

var args = page.url.args;

var navMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");


navMenu.config.widgets.push({
    id: "HEADER_CASE_DASHBOARD",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_DASHBOARD",
        label: "header.document.case.dashboard" ,
        title: "header.document.case.dashboard.altText",
        targetUrl: "hdp/ws/dk-openesdh-pages-case-dashboard?nodeRef=" + args.nodeRef,
        selected: isCurrentUri("dk-openesdh-pages-case-dashboard")
    }
});

navMenu.config.widgets.push({
    id: "HEADER_CASE_DOCUMENTS",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_DOCUMENTS",
        label: "header.case.documents" ,
        title: "header.case.documents.altText",
        targetUrl: "hdp/ws/dk-openesdh-pages-documents?nodeRef=" + args.nodeRef,
        selected: isCurrentUri("dk-openesdh-pages-documents")
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
        targetUrl: "hdp/ws/dk-openesdh-pages-case-dashboard-members?nodeRef=" + args.nodeRef,
        selected: isCurrentUri("dk-openesdh-pages-case-dashboard-members")
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
        targetUrl: "edit-metadata?nodeRef=" + args.nodeRef,
        visibilityConfig: {
            initialValue: false,
            rules: [
                {
                    topic: "CASE_INFO",
                    attribute: "isJournalized",
                    isNot: [true]
                }
            ]
        }
    }
});

caseConfig.config.widgets.push({
    id: "HEADER_CASE_JOURNALIZE",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        label: "header.case.journalize",
        iconClass: "alf-cog-icon",

        publishTopic: "JOURNALIZE",
        publishPayload: {},
        visibilityConfig: {
            initialValue: false,
            rules: [
                {
                    topic: "CASE_INFO",
                    attribute: "isJournalized",
                    isNot: [true]
                }
            ]
        }
    }
});
caseConfig.config.widgets.push({
    id: "HEADER_CASE_UNJOURNALIZE",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        label: "header.case.unjournalize",
        iconClass: "alf-cog-icon",

        publishTopic: "UNJOURNALIZE",
        publishPayload: {},
        visibilityConfig: {
            initialValue: false,
            rules: [
                {
                    topic: "CASE_INFO",
                    attribute: "isJournalized",
                    is: [true]
                }
            ]
        }
    }
});

var verticalLayout = widgetUtils.findObject(model.jsonModel, "id", "SHARE_VERTICAL_LAYOUT");
verticalLayout.config.widgets.push({
    id: "HEADER_CASE_JOURNALIZED_WARNING",
    name: "alfresco/header/Warning",
    config: {
        warnings: [{
            message: msg.get("warning.case.journalized"),
            level: 1
        }],
        visibilityConfig: {
            initialValue: false,
            rules: [
                {
                    topic: "CASE_INFO",
                    attribute: "isJournalized",
                    is: [true]
                }
            ]
        }
    }
});

model.jsonModel.services.push({
    name: "openesdh/pages/case/services/Dashboard",
    config: {
        caseNodeRef: args.nodeRef
    }
});

navMenu.config.widgets.push(caseConfig);