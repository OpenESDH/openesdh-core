/*
 * From https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions
 */
function escapeRegExp(string){
    return string.replace(/([.*+?^${}()|\[\]\/\\])/g, "\\$1");
}


/**
 * Check if the current page is the given case page.
 */
function isOnCasePage(uri) {
    return page.url.uri.match(new RegExp("^.+/page/oe/case/.+?/" + escapeRegExp(uri)));
}

/**
 * Get the nodeRef of the user given by the username.
 * @returns {string}
 */
function getUserNodeRef(userName) {
    var connector = remote.connect("alfresco");
    var res = connector.get("/api/nodelocator/xpath?query=/sys:system/sys:people/cm:" + encodeURIComponent(userName));

    res = eval('(' + res + ')');
    return res.data.nodeRef;
}
