var headerMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
if (headerMenu != null) {
    widgetUtils.deleteObjectFromArray(headerMenu.config.widgets, "id", "HEADER_SITES_MENU");
    var test = widgetUtils.deleteObjectFromArray(headerMenu.config.widgets, "id", "HEADER_DAGSORDEN_MENU");
}
