var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
if (headerMenu != null) {
    widgetUtils.deleteObjectFromArray(headerMenu.config.widgets, "id", "HEADER_SITES_MENU");
    widgetUtils.deleteObjectFromArray(headerMenu.config.widgets, "id", "HEADER_TASKS");
    widgetUtils.deleteObjectFromArray(headerMenu.config.widgets, "id", "HEADER_PEOPLE");
    widgetUtils.deleteObjectFromArray(headerMenu.config.widgets, "id", "HEADER_ADMIN_CONSOLE");
    widgetUtils.deleteObjectFromArray(headerMenu.config.widgets, "id", "HEADER_DAGSORDEN_MENU");
}
