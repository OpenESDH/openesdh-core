//var title = widgetUtils.findObject(model.jsonModel, "id", "HEADER_TITLE");
//title.config.label = "header.title";

var args = page.url.args;

var navMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");



navMenu.config.widgets.push({
    id: "HEADER_DOCUMENT_CREATE",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_DOCUMENT_CREATE",
        label: "header.document.create" ,
        title: "header.document.create.altText",
        targetUrl: "create-content?destination=" + encodeURIComponent(args.nodeRef) + "&itemId=" + encodeURIComponent("doc:simple")
    }
});

navMenu.config.widgets.push({
    id: "HEADER_CASE_DASHBOARD",
    name: "alfresco/menus/AlfMenuBarItem",
    config: {
        id: "HEADER_CASE_DASHBOARD",
        label: "header.document.case.dashboard" ,
        title: "header.document.case.dashboard.altText",
//        title: "header.case.members.title",
        targetUrl: "hdp/ws/dk-openesdh-pages-case-dashboard?nodeRef=" + args.nodeRef
    }
});
