define(["dojo/_base/declare",
        "alfresco/lists/views/AlfListView",
        "alfresco/core/Core",
        "dojo/_base/lang",
        "alfresco/core/I18nUtils"
        ],
        function(declare, AlfListView, Core, lang, I18nUtils){
            var i18nScope = "openesdh.case.CaseDocumentsList";
            return declare([Core, AlfListView], {
                
                i18nScope: i18nScope,
                
                i18nRequirements: [{i18nFile: "./i18n/CaseDocumentsList.properties"}],
                        
                additionalCssClasses: "bordered",
                
                widgetsForHeader: [
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.title.label")
                        }
                    },
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.type.label")
                        }
                    },
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.category.label")
                        }
                    },
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.state.label")
                        }
                    },
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.owner.label")
                        }
                    },
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.created.label")
                        }
                    },
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.modified.label")
                        }
                    },
                    {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: I18nUtils.msg(i18nScope, "header.column.action.label")
                        }
                    }, {
                        name: "alfresco/lists/views/layouts/HeaderCell",
                        config: {
                            label: "",
                        }
                }], //END OF widgetsForHeader
                        
                widgets: [{
                    name: "alfresco/lists/views/layouts/Row",
                    config: {
                        widgets: [{
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "alfresco/renderers/Property",
                                    config: {
                                        propertyToRender: "cm:name"
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "openesdh/common/widgets/renderers/DocumentType",
                                    config: {
                                        propertyToRender: "doc:type"
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "openesdh/common/widgets/renderers/DocumentCategory",
                                    config: {
                                        propertyToRender: "doc:category"
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "openesdh/common/widgets/renderers/DocumentState",
                                    config: {
                                        propertyToRender: "doc:state"
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "openesdh/common/widgets/renderers/UserNameField",
                                    config: {
                                        propertyToRender: "doc:owner"
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "alfresco/renderers/Date",
                                    config: {
                                        propertyToRender: "cm:created",
                                        simple: true
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "alfresco/renderers/Date",
                                    config: {
                                        propertyToRender: "cm:modified",
                                        simple: true
                                    }
                                }]
                            }
                        },
                        {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                additionalCssClasses: "mediumpad",
                                widgets: [{
                                    name: "alfresco/renderers/PropertyLink",
                                    config: {
                                        renderedValue: I18nUtils.msg(i18nScope, "case.doc.details.action.label"),
                                        publishTopic: "OPENESDH_CASE_DOC_DETAILS"
                                    }
                                }]
                            }
                        }, {
                            name: "alfresco/lists/views/layouts/Cell",
                            config: {
                                widgets: [{
                                    name: "alfresco/renderers/PropertyLink",
                                    config: {
                                        renderedValue: I18nUtils.msg(i18nScope, "case.doc.preview.action.label"),
                                        publishTopic: "OPENESDH_CASE_DOC_PREVIEW"
                                    }
                                }]
                            }
                        }]
                    }
                }] // END OF widgets
            });
        });