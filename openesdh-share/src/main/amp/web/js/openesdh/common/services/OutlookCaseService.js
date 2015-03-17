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

                this.getSearchDefinition();
                this.alfSubscribe("OE_FIND_CASE", lang.hitch(this, this.onFindCase));
                this.alfSubscribe("GRID_ROW_SELECTED", lang.hitch(this, this.onSelectCase));
            },

            getSearchDefinition: function() {
                this.serviceXhr({
                    url: Alfresco.constants.PROXY_URI + "api/openesdh/case/searchDefinition/case_simple",
                    method: "GET",
                    handleAs: "json",
                    successCallback: function (response, config) {
                        this.searchDefinition = response;
                    },
                    failureCallback: function(response, config){
                        alert("failure");
                    },
                    callbackScope: this
                });
            },
//            _getDataObject: function() {
//                return {
//                    caseId: this.caseId,
//                    subject: this.subject,
//                    attachments: this.attachments
//                };
//            },

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
                        alert(response);
                    }
                });
            },

            _onCancel: function() {
                window.external.CancelOpenEsdh();
            },

            onFindCase: function() {
                this.alfPublish("ALF_CREATE_DIALOG_REQUEST", {
                    contentWidth: "800px",
                    dialogTitle: "Find Case",
                    hideTopic: "GRID_ROW_SELECTED",
                    widgetsContent: [
                        {
                            name: 'alfresco/layout/VerticalWidgets',
                            config: {
                                widgets: [
                                    {
                                        name: "openesdh/xsearch/FilterPane",
                                        config: {
                                            baseType: "case:base",
                                            types: this.searchDefinition.model.types,
                                            properties: this.searchDefinition.model.properties,
                                            availableFilters: this.searchDefinition.availableFilters,
                                            operatorSets: this.searchDefinition.operatorSets
                                        }
                                    },
                                    {
                                        name: "openesdh/pages/case/widgets/MyCasesWidget",
                                        config: {
                                            actions : [],
                                            getColumns: function () {
                                                return [
                                                    { field: "oe:id", label: this.message("mycases.column.id")  },
                                                    { field: "cm:title", label: "Title"  },
                                                    { field: "oe:status", label: this.message("mycases.column.state")},
                                                    { field: "cm:modified", label: this.message("mycases.column.modified"),
                                                        formatter: lang.hitch(this, "_formatDate")
                                                    }
                                                ];
                                            }
                                        }
                                    }
                                ]
                            }
                        }
                    ],
                    widgetsButtons: [
                        {
                            name: "alfresco/buttons/AlfButton",
                            config: {
                                label: "Cancel",
                                publishTopic: "NO_OP"
                            }
                        }
                    ]
                });
            },

            onSelectCase: function(payload) {
                var data = payload.row.data;
                this._getForm().setValue({caseId: data["oe:caseId"]});
            }
        });
    }
);