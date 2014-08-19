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
    return "TODO";
}

/**
 * Get the case types
 */
function getCaseTypes () {
    // TODO: Call model webscript
    return {
        "case:simple": {
            'label': 'Simple Case',
            'roles': []
        }
    }
}