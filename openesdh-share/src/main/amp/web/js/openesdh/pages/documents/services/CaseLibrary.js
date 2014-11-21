define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin"],
    function (declare, AlfCore, CoreXhr, array, lang, _TopicsMixin) {

        return declare([AlfCore, CoreXhr, _TopicsMixin], {
            caseNodeRef: "",

            constructor: function (args) {
                console.log("CONSTRUCTOR CaseLibrary Service");

                this.caseNodeRef = args.caseNodeRef;
                this.alfSubscribe("CASE_LIBRARY_GET_DOC_NODEREF", lang.hitch(this, this._documentsNodeRef));
            },

            _documentsNodeRef: function () {
                console.log("GET documents noderef");
                var nodeRef = NodeUtils.processNodeRef(this.caseNodeRef),
                    targetNodeUri = nodeRef.uri;

                // Get documents from webscript
                var url = Alfresco.constants.PROXY_URI + "/api/openesdh/caselib/docNode/" + this.targetNodeUri;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: this._onSuccessCallback,
                    callbackScope: this});
            },

            _onSuccessCallback: function (response, config) {
                console.log("CALLBACK doc nodeRef");

                this.documentsNodeRef = response.documentsNodeRef;

                var domReadyFunction = (function (scope) {
                    return function () {
                        console.log("PUBLISH documents");
                        scope.alfPublish(scope.CaseDocNodeRefTopic, response.node);
                    }
                })(this);

            }
        });
    });