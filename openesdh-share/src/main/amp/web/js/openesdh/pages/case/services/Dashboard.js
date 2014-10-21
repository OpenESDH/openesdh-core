define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"],
    function (declare, AlfCore, CoreXhr, array, lang, _TopicsMixin ) {

        return declare([AlfCore, CoreXhr, _TopicsMixin],  {

            destinationNodeRef: null,

            caseNodeRef: "",

            constructor: function (args) {
                lang.mixin(this, args);
                this.caseNodeRef = args.caseNodeRef;
                this._caseInfo(this.caseNodeRef);

                this.alfSubscribe("JOURNALIZE", lang.hitch(this, "_onJournalize"));
            },

            _caseInfo: function (nodeRef) {
                // Get caseInfo from webscript
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/caseinfo?nodeRef=" + nodeRef;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: this._onCaseInfoSuccessCallback,
                    callbackScope: this});
            },

            _onCaseInfoSuccessCallback: function (response, config) {

                var domReadyFunction = (function (scope) {
                    return function () {
                        scope.alfPublish(scope.CaseInfoTopic, response);
                        scope.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: response["cm:title"].value});
                    }
                })(this);

                require(["dojo/ready"], function(ready) {
                    // will not be called until DOM is ready
                    ready(domReadyFunction);
                });
            },

            _onJournalizeSuccessCallback: function (response, config) {

                Alfresco.util.PopupManager.displayMessage(
                        {
                            text: this.message('journalize.success')
                        });

                window.location.reload();
            },

            _onJournalize: function () {
                this.widgets = [];

                var url = Alfresco.constants.PROXY_URI + "api/openesdh/journalize?nodeRef=" + this.caseNodeRef + "&journalKey=workspace://SpacesStore/8544ad16-e88f-4dce-979e-1eff675262ee";
                this.serviceXhr({
                    url: url,
                    method: "PUT",
                    successCallback: this._onJournalizeSuccessCallback,
                    callbackScope: this});
            }
        });
    });