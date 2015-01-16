
/**
 * Alfresco Slingshot aliases
 */
var $siteURL = Alfresco.util.siteURL;

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
            return responseObject.caseId;
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

    console.log("Exiting");
};

/**
 * Displays the corresponding details page for the current node
 *
 * @method _navigateForward
 * @private
 * @param nodeRef {Alfresco.util.NodeRef} Optional: NodeRef of just-created content item
 */
Alfresco.CreateContentMgr.prototype._navigateForward = function CreateContentMgr__navigateForward(nodeRef) {
    console.log("Creating content")
    /* Have we been given a nodeRef from the Forms Service? */
    if (YAHOO.lang.isObject(nodeRef)) {
        //window.location.href = $siteURL((this.options.isContainer ? "folder" : "document") + "-details?nodeRef=" + nodeRef.toString());
        window.location.href = caseURL("case",this._getCaseId(nodeRef), "dashboard");
    }
    else if (document.referrer) {
        /* Did we come from the document library? If so, then direct the user back there */
        if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/)) {
            // go back to the referrer page
            history.go(-1);
        }
        else {
            document.location.href = document.referrer;
        }
    }
    else if (this.options.siteId && this.options.siteId !== "") {
        // In a Site, so go back to the document library root
        window.location.href = $siteURL("documentlibrary");
    }
    else {
        window.location.href = Alfresco.constants.URL_CONTEXT;
    }
};
