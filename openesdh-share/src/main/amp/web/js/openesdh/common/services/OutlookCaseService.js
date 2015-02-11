define([
    "dojo/_base/declare",
    "aoi/common/services/OfficeIntegrationService",
    "alfresco/core/CoreXhr",
    "dojo/_base/lang",
    "service/constants/Default"
    ],
    function(declare, OfficeIntegrationService, CoreXhr, lang, AlfConstants) {
        return declare([OfficeIntegrationService, CoreXhr], {

            constructor: function(args) {
                lang.mixin(this, args);
            },

            _onOK: function() {
                var value = this._getForm().getValue();
//                alert("caseId: " + value["caseId"] + ", title: " + value["title"]);
                this.serviceXhr({
                    url: AlfConstants.PROXY_URI + "dk-openesdh-case-email",
                    method: "POST",
                    data: {
                        caseId: value["caseId"],
                        name: value["title"],
                        responsible: value["responsible"],
                        email: this.emailDesc
                    },
                    successCallback: function(response, originalRequestConfig) {
//                        alert("success!");
                        var metadata = {
                            nodeRef: nodeRef
                        };
//                        window.external.SaveAsOpenEsdh(JSON.stringify(metadata), JSON.stringify(value.attachments));
                    },
                    failureCallback: function(response, originalRequestConfig) {
//                        alert("failure!");
                        alert(response);
                    }
                });
            },
            _onCancel: function() {
                alert("Cancel button pressed.");
//                window.external.SaveAsOpenEsdh("", "");
            }
        });
    }
);