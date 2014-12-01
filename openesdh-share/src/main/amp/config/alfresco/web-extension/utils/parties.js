/**
 * Get the nodeRef of the parties folder.
 * @returns {string}
 */
function getPartiesFolderNodeRef() {
    var connector = remote.connect("alfresco");
    var result = connector.get("/api/nodelocator/xpath?query=/app:company_home/app:dictionary/party:parties");

    result = eval('(' + result + ')');
    return result.data.nodeRef;
}