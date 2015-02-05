var args = page.url.args;

var navMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");


navMenu.config.widgets.push({
    id: "HEADER_CASE_DASHBOARD",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_DASHBOARD",
        label: "header.document.case.dashboard" ,
        title: "header.document.case.dashboard.altText",
//        title: "header.case.members.title",
        targetUrl: "hdp/ws/dk-openesdh-pages-case-dashboard?nodeRef=" + args.caseNodeRef
    }
});


// Create the create documents menu...
var createDocument = {
    id: "HEADER_DOCUMENT_CREATE_DROPDOWN",
    name: "alfresco/menus/AlfMenuBarPopup",
    config: {
        id: "HEADER_DOCUMENT_CREATE_DROPDOWN",
        label: "header.document.create" ,
        title: "header.document.create.altText",
        widgets: []
    }
};

createDocument.config.widgets.push({
    id: "HEADER_DOCUMENT_CREATE_DIGITAL",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_DOCUMENT_CREATE_DIGITAL",
        label: "header.document.create.digital" ,
        publishTopic: "OE_SHOW_UPLOADER",
        publishPayload: {}
//        targetUrl: "create-content?destination=" + encodeURIComponent(args.nodeRef) + "&itemId=" + encodeURIComponent("doc:simple")
    }
});

createDocument.config.widgets.push({
    id: "HEADER_DOCUMENT_CREATE_PHYSICAL",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_DOCUMENT_CREATE_PHYSICAL",
        label: "header.document.create.physical" ,
        targetUrl: "/dashboard"
    }
});

createDocument.config.widgets.push({
    id: "TEST",
    name: "alfresco/upload/AlfUpload"
});

navMenu.config.widgets.push(createDocument);
