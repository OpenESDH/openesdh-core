define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"],
    function (declare, AlfCore, CoreXhr, array, lang, _TopicsMixin) {

        return declare([AlfCore, CoreXhr, _TopicsMixin], {

            destinationNodeRef: null,

            nodeRef: "",

            constructor: function (args) {
                lang.mixin(this, args);
                this._caseInfo(args.nodeRef);
            },

            _caseInfo: function (nodeRef) {
                // Get caseInfo from webscript
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/caseinfo?nodeRef=" + nodeRef;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: this._onSuccessCallback,
                    callbackScope: this});
            },

            _onSuccessCallback: function (response, config) {
                this.alfPublish(this.CaseInfoTopic, response);
            }
        });
    });