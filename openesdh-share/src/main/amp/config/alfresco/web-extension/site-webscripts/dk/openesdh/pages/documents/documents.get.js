<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/pages/documents/lib/case-library.lib.js">
//<import resource="classpath:/alfresco/web-extension/utils/case.js">

/**
 * Get the role types for a case
 */
    function getCaseDocumentNodeRef(nodeRef) {

    var caseDocNode = nodeRef.replace("://", "/");
    var connector = remote.connect("alfresco");
    var docNode = connector.get("/api/openesdh/caselib/docNode/" +caseDocNode);

    var caseDocNodeRef = eval('(' + docNode + ')');
    return caseDocNodeRef.node;
}

// Ideally we'd build the array of widgets to go in the main vertical stack starting with the header widgets
// and then adding in the document library widgets. However, this won't be possible until either the full-page.get.html.ftl
// template has been updated to include all of the "legacy" resources (e.g. YAHOO), or they have been explicitly requested
// as non-AMD dependencies in the widgets referenced on the page. In the meantime this page will be rendered as a hybrid.
// var widgets = getHeaderModel().concat([getDocumentLibraryModel("", "", user.properties['userHome'])]);

var tmp = url.args.nodeRef;

var documentNode = getCaseDocumentNodeRef(tmp);
var services = getDocumentLibraryServices(null, null, documentNode);
var widgets = [getDocumentLibraryModel(null, null, documentNode)];

// Change the root label of the tree to be "My Files" rather than "Documents"
var tree = widgetUtils.findObject(widgets, "id", "DOCLIB_TREE");
if (tree != null) {
    tree.config.rootLabel = "my-files.root.label";
}

model.jsonModel = {
    services: services,
    widgets: [
        {
            id: "SET_PAGE_TITLE",
            name: "alfresco/header/SetTitle",
            config: {
                widgetMarginLeft: 10,
                widgetMarginRight: 10,
                widgets: [
                    {
                        name: "openesdh/pages/documents/widgets/DocumentList",
                        config: {
                            title: "Hej"
                        }
                    }
                ]

            }
        },
        {
            id: "SHARE_VERTICAL_LAYOUT",
            name: "alfresco/layout/VerticalWidgets",
            config:
            {
                widgets: widgets
            }
        }
    ]
};

