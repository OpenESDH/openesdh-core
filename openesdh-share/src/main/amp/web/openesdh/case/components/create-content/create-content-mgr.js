
/**
 * Alfresco Slingshot aliases
 */
var $siteURL = Alfresco.util.siteURL;
Alfresco.CreateContentMgr.prototype.caseId = "";
//Attempt to duplicate the above
var caseURL = function(pageURI, id ,webscript, obj, absolute) {
    return Alfresco.util.uriTemplate("casepage", YAHOO.lang.merge(obj || {},
        {
            pageid: pageURI,
            caseId: id,
            webscript: webscript
        }), absolute);
};

Alfresco.CreateContentMgr.prototype._getCaseId = function CreateContentMgr_getCaseId(nodeRef){
        // construct the url to call
        var url = Alfresco.constants.PROXY_URI + "/api/openesdh/documents/isCaseDoc/"+nodeRef.uri;

        var caseIdRetrievedSuccess = function(response){
            console.log("about to return");
            var responseObject= eval('('+response.serverResponse.responseText+')');
            window.location.href = caseURL("case", responseObject.caseId, "documents");
        };

        // execute ajax request
        Alfresco.util.Ajax.request({
                url: url,
                successCallback:{
                    fn: caseIdRetrievedSuccess,
                    scope: this
                },//TODO read the line below
                failureMessage: this.msg("localise this message about not being able to get the case id for the created content")
        });
};

//TODO for now all content creation redirects to the case dashboard. Is this to be the final behaviour
/**
 * Displays the corresponding details page for the current node
 *
 * @method _navigateForward
 * @private
 * @param nodeRef {Alfresco.util.NodeRef} Optional: NodeRef of just-created content item
 */
Alfresco.CreateContentMgr.prototype._navigateForward = function CreateContentMgr__navigateForward(nodeRef) {
    /* Have we been given a nodeRef from the Forms Service? */
    if (YAHOO.lang.isObject(nodeRef)) {
        this._getCaseId(nodeRef);

    }

};
