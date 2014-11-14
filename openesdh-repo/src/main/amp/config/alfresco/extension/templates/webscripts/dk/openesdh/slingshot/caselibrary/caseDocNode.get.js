/**
 * Created by lanre on 10/11/2014.
 */

/**
 * Returns the nodeRef of the document folder for a case
 */

function main(){
    var storeType = url.templateArgs.store_type,
        storeId = url.templateArgs.store_id,
        id = url.templateArgs.id,
        nodeRef = storeType + "://" + storeId + "/" + id,
        caseNode = utils.getNodeFromString(nodeRef);

    if (caseNode === null && caseNode === undefined) {
        logger.error("\n\n ======> unable to find case. <======\n\n");
        status.setCode(status.STATUS_BAD_REQUEST, "The nodeRef for the case is invalid or no longer exists");
        return;
    }
    var caseFolders = caseNode.childFileFolders(false, true);
    var documentFolder;

    caseFolders.forEach(function(node){ if(node.properties.name == "documents") documentFolder = node;});

    (documentFolder != null) ? model.item = documentFolder : "";
}


main();