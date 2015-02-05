/**
 * Get the nodeRef of the folder to store new cases in.
 * @returns {string}
 */
function getNewCaseFolderNodeRef () {

    var connector = remote.connect("alfresco");
    var companyHome = connector.get("/api/nodelocator/userhome");

    companyHome = eval('(' + companyHome + ')');
    companyHome = companyHome["data"];

    return companyHome["nodeRef"];
}

/**
 * Get the case types
 */
function getCaseTypes () {

    var connector = remote.connect("alfresco");
    var caseTypes = connector.get("/api/openesdh/casetypes");

    caseTypes = eval('(' + caseTypes + ')');
    var casesArr = new Array();

    for (var i in caseTypes) {

        var c = caseTypes[i];
        var cObject = {};
        cObject["type"] = c.Prefix;
        cObject["label"] = c.Title;
        casesArr.push(cObject);
    }
    return casesArr;
}

/**
 * Get the role types for a case
 */
function getCaseRoleTypes (nodeRef, caseId) {

    var connector = remote.connect("alfresco");
    var roleTypes = connector.get("/api/openesdh/"+caseId+"/caseroles?nodeRef=" + encodeURIComponent(nodeRef));
    roleTypes = eval('(' + roleTypes + ')');
    return roleTypes;
}

/**
 * Get the case nodeRef from caseId
 */
function getCaseNodeRefFromId(caseId){
    var connector = remote.connect("alfresco");
    var caseInfo = connector.get("/api/openesdh/case/noderef/"+caseId);
    caseInfo = eval('(' + caseInfo + ')');

    return caseInfo.caseNodeRef;
}