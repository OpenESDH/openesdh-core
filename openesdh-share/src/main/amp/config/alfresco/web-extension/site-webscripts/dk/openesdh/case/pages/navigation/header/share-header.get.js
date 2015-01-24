<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/utils/oe.js">
<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/global-customisations/components/console/users/contact-users.lib.js">

var args = page.url.args;
var caseId = url.templateArgs.caseId;

var navMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");

navMenu.config.widgets.push({
    id: "HEADER_CASE_DASHBOARD",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_DASHBOARD",
        label: "header.document.case.dashboard" ,
        title: "header.document.case.dashboard.altText",
        targetUrl: "oe/case/"+caseId+"/dashboard",
        selected: isCurrentUri("dashboard")
    }
});

navMenu.config.widgets.push({
    id: "HEADER_CASE_DOCUMENTS",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_DOCUMENTS",
        label: "header.case.documents" ,
        title: "header.case.documents.altText",
        targetUrl: "oe/case/"+caseId+"/documents",
        selected: isCurrentUri("documents")
    }
});

//TODO The link to this page does not work. The service call for the data is not correct. Why is this so?
navMenu.config.widgets.push({
    id: "HEADER_CASE_MEMBERS",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_MEMBERS",
        iconClass: "alf-user-icon",
        iconAltText: "header.case.members.altText",
//        title: "header.case.members.title",
        targetUrl: "oe/case/"+caseId+"/members",
        selected: isCurrentUri("members")
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
                    attribute: "canJournalize",
                    is: [true]
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
                    attribute: "canUnJournalize",
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
        caseId: caseId,
        nodeRef: (args.nodeRef != null) ? args.nodeRef : args.destination
    }
});

//The next two lines are just for testing the dialog form for contact and user creation. (Think checkbox visibility)
//model.jsonModel.services.push({name: "alfresco/dialogs/AlfDialogService"});
//navMenu.config.widgets.push(generateContactPageWidgets());

navMenu.config.widgets.push(caseConfig);