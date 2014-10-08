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

                console.log("CONSTRUCTOR documents");

                lang.mixin(this, args);
                this._documents(args.nodeRef);
            },

            _documents: function (nodeRef) {
                console.log("GET documents");
                // Get documents from webscript
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/documents?nodeRef=" + nodeRef;
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: this._onSuccessCallback,
                    callbackScope: this});
            },

            _onSuccessCallback: function (response, config) {
                console.log("CALLBACK documents");

                var domReadyFunction = (function (scope) {
                    return function () {
                        console.log("PUBLISH documents");
                        scope.alfPublish(scope.DocumentsTopic, response);
                        scope.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: response["caseTitle"]});
                    }
                })(this);

                require(["dojo/ready"], function(ready) {
                    // will not be called until DOM is ready
                    ready(domReadyFunction);
                });
            }
        });
    });