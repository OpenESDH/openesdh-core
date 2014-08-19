define(["dojo/_base/declare",
    "alfresco/core/Core",
    "alfresco/core/CoreXhr",
    "dojo/_base/array",
    "dojo/_base/lang"],
function(declare, AlfCore, CoreXhr, array, lang) {

    return declare([AlfCore, CoreXhr], {
        destinationNodeRef: null,
        constructor: function(args) {
            lang.mixin(this, args);
            
            console.log("CaseNavigationService start");

            var _this = this;

            this.alfSubscribe("CASE_NEW_CASE", lang.hitch(this, "newCase"));
        },
        
        newCase: function(payload) {
            var caseType = typeof payload.caseType !== 'undefined' ? payload.caseType : 'esdh:case';
            var destination = payload.destination;
            if (destination !== 'undefined') {
                this.navigate(this.getNewCaseURL(destination, caseType));
            } else {
                throw "CASE_NEW_CASE handler: 'destination' parameter not set";
            }
        },
        getNewCaseURL: function(destination, caseType) {
            return Alfresco.constants.URL_PAGECONTEXT + "create-content?destination=" + destination + "&itemId=" + encodeURIComponent(caseType);
        },
        navigate: function(url) {
            window.location = url;
        }
    });
});