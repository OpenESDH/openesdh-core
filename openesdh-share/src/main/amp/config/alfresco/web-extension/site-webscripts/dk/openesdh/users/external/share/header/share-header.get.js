var searchMenu = widgetUtils.findObject(model.jsonModel.widgets, "id", "HEADER_SEARCH_BOX");
var headerMenu = widgetUtils.findObject(model.jsonModel.widgets, "id", "HEADER_APP_MENU_BAR");
var headerTitleMenu = widgetUtils.findObject(model.jsonModel.widgets, "id", "HEADER_TITLE_MENU"); //For the dashboard config

if(headerMenu){
    headerMenu.config.widgets = [];
    headerMenu.config.widgets.push({
        id: "HEADER_HOME",
        name: "alfresco/menus/AlfMenuBarItem",
        config: {
            id: "HEADER_HOME",
            label: "header.menu.home.label",
            targetUrl: "user/" + encodeURIComponent(user.name) + "/dashboard"
        }
    });
}

if(searchMenu){
   widgetUtils.deleteObjectFromArray(model.jsonModel, "id", searchMenu.id);
}
if(headerTitleMenu){
   widgetUtils.deleteObjectFromArray(model.jsonModel, "id", headerTitleMenu.id);
}

