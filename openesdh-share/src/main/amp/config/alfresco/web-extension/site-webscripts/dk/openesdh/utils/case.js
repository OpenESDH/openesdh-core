/**
 * Get the nodeRef of the folder to store new cases in.
 * @returns {string}
 */
function getNewCaseFolderNodeRef() {

    var connector = remote.connect("alfresco");
    var companyHome = connector.get("/api/nodelocator/userhome");

    companyHome = eval('(' + companyHome + ')');
    companyHome = companyHome["data"];

    return companyHome["nodeRef"];
}

/**
 * Get the case types
 */
function getCaseTypes() {

    var connector = remote.connect("alfresco");
    var caseTypes = connector.get("/api/openesdh/casetypes");

    caseTypes = eval('(' + caseTypes + ')');
    var casesArr = new Array();

    for (var i in caseTypes) {

        var c = caseTypes[i];
        var cObject = {};
        cObject["type"] = c.Prefix; //The actual model type containing the namespace prefix and postfix (base:case) e.g.
        cObject["typeName"] = c.Type; //The type name (The namespace prefix for the type)
        cObject["label"] = c.Title;
        cObject["createForm"] = c.createFormWidgets;
        casesArr.push(cObject);
    }
    return casesArr;
}

/**
 * Get the role types for a case
 */
function getCaseRoleTypes(nodeRef, caseId) {

    var connector = remote.connect("alfresco");
    var roleTypes = connector.get("/api/openesdh/" + caseId + "/caseroles?nodeRef=" + encodeURIComponent(nodeRef));
    roleTypes = eval('(' + roleTypes + ')');
    return roleTypes;
}


/**
 * Get the permitted role types for cases. Used for the contact roles
 */
function getPermittedRoleTypes() {

    var connector = remote.connect("alfresco");
    var roleTypes = connector.get("/api/openesdh/case/party/permittedRoles");
    roleTypes = eval('(' + roleTypes + ')');
    return roleTypes;
}

/**
 * Get the permitted status types for cases. Used for the case status select control input(s)
 */
//TODO Don't think we need this any longer
function getCaseStatusTypes() {
    var connector = remote.connect("alfresco");
    var states = connector.get("/api/openesdh/case/party/permittedStates");
    states = eval('(' + states + ')');
    return states;
}

/**
 * Get the case nodeRef from caseId
 */
function getCaseNodeRefFromId(caseId) {
    //When we're not within the case context this will always be null as CaseService is included in the
    if (caseId == null || caseId == undefined)
        return null;
    var connector = remote.connect("alfresco");
    var caseInfo = connector.get("/api/openesdh/case/noderef/" + caseId);
    caseInfo = eval('(' + caseInfo + ')');

    return caseInfo.caseNodeRef;
}

/**
 * Get the case id from nodeRef
 */
function getCaseIdFromNodeRef(nodeRef) {
    if (nodeRef == null || nodeRef == undefined)
        return null;
    var caseId = nodeRef.replace(":/", "");
    var connector = remote.connect("alfresco");
    var caseInfo = connector.get("/api/openesdh/documents/isCaseDoc/" + caseId);
    caseInfo = eval('(' + caseInfo + ')');

    return caseInfo.caseId;
}

function startsWith(str, prefix) {
    if (!(prefix instanceof Array)) {
        return str.lastIndexOf(prefix, 0) === 0;
    }
    for (var i in prefix) {
        var p = prefix[i];
        if (str.lastIndexOf(p, 0) === 0) {
            return true;
        }
    }
    return false;
}

function endsWith(str, suffix) {
    if (!(suffix instanceof Array)) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }
    for (var i in suffix) {
        var s = suffix[i];
        if (str.indexOf(s, str.length - s.length) !== -1) {
            return true;
        }
    }
    return false;
}

/**
 * Checks whether current user has write permissions for the case
 */
function hasWritePermission(caseId) {
    var connector = remote.connect("alfresco");
    var userPermissions = connector.get("/api/openesdh/case/" + caseId + "/user/permissions");
    userPermissions = eval('(' + userPermissions + ')');

    for (var i in userPermissions) {
        var permission = userPermissions[i]
        if (startsWith(permission, "Case") && endsWith(permission, ["Writer", "Owners"])) {
            return true;
        }
    }

    return false;
}

//We use this for the authority picker as the  user is returned in the required format
function getCurrentUser() {
    var connector = remote.connect("alfresco");
    var currentUser = connector.get("/api/openesdh/currentUser");
    return eval('(' + currentUser + ')');
}
function getCaseConstraints() {
    var connector = remote.connect("alfresco");
    var constraints = connector.get("/api/openesdh/case/constraints");
    constraints = eval('(' + constraints + ')');
    return constraints;
}