/**
 * Get the nodeRef of the parties folder.
 * @returns {string}
 */
function getContactsFolderNodeRef() {
    var connector = remote.connect("alfresco");
    var result = connector.get("/api/nodelocator/xpath?query=/app:company_home/app:dictionary/contact:contacts");

    result = JSON.parse(result);
    return result.data.nodeRef;
}
