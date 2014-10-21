function main() {
    // Widget instantiation metadata...
    var widget = {
        id : "CreateContentMgr",
        name : "OpenESDH.CreateContentMgr",
        options : {
            siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
            isContainer: "true" == ((page.url.args.isContainer != null) ? page.url.args.isContainer : "false"),
            itemId: (page.url.args.itemId != null) ? page.url.args.itemId : ""
        }
    };
    model.widgets = [widget];
}
main();
