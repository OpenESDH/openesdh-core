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
                this.serviceXhr({
                    url: AlfConstants.PROXY_URI + "dk-openesdh-case-email",
                    method: "POST",
                    data: {
                        caseId: value["caseId"],
                        name: value["subject"],
                        responsible: value["responsible"],
                        email: this.emailDesc
                    },
                    successCallback: function(response, originalRequestConfig) {
                        var metadata = {
                            nodeRef: response["nodeRef"]
                        };
                        window.external.SaveAsOpenEsdh(JSON.stringify(metadata), JSON.stringify(value.attachments));
                    },
                    failureCallback: function(response, originalRequestConfig) {
//                        alert("failure!");
                        alert(response);
                    }
                });
            },
            _onCancel: function() {
                window.external.CancelOpenEsdh();
            }
        });
    }
);