var itemType = decodeURIComponent(url.args.itemId);
var createWidget = widgetUtils.findObject(model.widgets, "id", "CreateContentMgr");

function startsWithCase(string){
    var comparator = "case:";
    return comparator == string.slice(0,5);
}

if(startsWithCase(itemType))
    if(createWidget != null){
        createWidget.options.isCase = true;
    }

