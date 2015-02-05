<import resource="classpath:/alfresco/web-extension/site-webscripts/dk/openesdh/case/documents/lib/case-library.lib.js">

/**
 * Get the case nodeRef from the case Id
 * @param caseId
 * @returns {case nodeRef}
 */
function getCaseNodeRef(caseId) {
    var connector = remote.connect("alfresco");
    var caseNode = connector.get("/api/openesdh/case/noderef/" +caseId);
    var caseNodeRef = eval('(' + caseNode + ')');

    return caseNodeRef.caseNodeRef;
}

/**
 * Gets the nodeRef of the documents library for the case
 * (Allows us to use the doclib widget)
 * @param nodeRef
 * @returns {*}
 */
function getCaseDocumentNodeRef(nodeRef) {

    var caseDocNode = nodeRef.replace("://", "/");
    var connector = remote.connect("alfresco");
    var docNode = connector.get("/api/openesdh/caselib/docNode/" +caseDocNode);

    var caseDocNodeRef = eval('(' + docNode + ')');
    return caseDocNodeRef.node;
}

var caseId = url.templateArgs.caseId;
var tmp = getCaseNodeRef(caseId);

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
            id: "SHARE_VERTICAL_LAYOUT",
            name: "alfresco/layout/VerticalWidgets",
            config:
            {
                widgets: widgets
            }
        }
    ]
};
