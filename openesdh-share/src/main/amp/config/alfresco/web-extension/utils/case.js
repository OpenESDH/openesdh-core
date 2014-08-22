

/**
 * Get the localized label for the case type
 * @param type
 * @returns {*}
 */
function getCaseTypeLabel (type) {
    // TODO
    return type;
}

/**
 * Get the nodeRef of the folder to store new cases in.
 * @returns {string}
 */
function getNewCaseFolderNodeRef () {

    var connector = remote.connect("alfresco");
    var companyHome = connector.get("/api/nodelocator/companyhome");

    companyHome = eval('(' + companyHome + ')');
    companyHome = companyHome["data"];

    return companyHome["nodeRef"];;
}

/**
 * Get the case types
 */
function getCaseTypes () {

    // TODO: Call model webscript
    /*
    var connector = remote.connect("alfresco");
    var caseTypes = connector.get("/api/openesdh/casetypes");

    caseTypes = eval('(' + caseTypes + ')');

    var casesArr = new Array();

    for (var i in caseTypes) {
        var c = caseTypes[i];
        var cObject = {};
        cObject["type"] = c.prefix;
        cObject["label"] = c.Name + " case";
        casesArr.push(cObject);
    }
    return casesArr;
    */




    return {
        "case:simple": {
            'label': 'Simple Case',
            'roles': []
        }

    }
}