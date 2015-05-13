define([
    "dojo/_base/declare",
    "aoi/common/services/OfficeIntegrationService",
    "alfresco/core/CoreXhr",
    "dojo/_base/lang",
    "service/constants/Default",
    "openesdh/xsearch/_TopicsMixin",
    "openesdh/pages/_TopicsMixin"
    ],
    function(declare, OfficeIntegrationService, CoreXhr, lang, AlfConstants, _TopicsMixin, _PagesTopicsMixin) {
        return declare([OfficeIntegrationService, CoreXhr, _TopicsMixin, _PagesTopicsMixin], {

            constructor: function(args) {
                lang.mixin(this, args);

                this.alfSubscribe("OE_FIND_CASE", lang.hitch(this, this.onFindCase));
                this.alfSubscribe("OE_CREATE_CASE", lang.hitch(this, this.onCreateCase));
                this.alfSubscribe("GRID_ROW_SELECTED", lang.hitch(this, this.onSelectCase));
            },
            
            _getDataObject: function() {
                return {
                    caseId: this.caseId,
                    title: this.documentDesc.Title
                };
            },

            _onLoad: function(payload) {
                alert("_onLoad");
                if (typeof payload == "string") {
                    this.documentDesc = JSON.parse(payload);
                } else {
                    this.documentDesc = payload;
                }

//                this.title = this.documentDesc.Title;
//                this.nodeRef = this.documentDesc.ID;

                this._getForm().setValue(this._getDataObject());
            },

            _onOK: function() {
                var value = this._getForm().getValue();
                var metadata = {
                    caseId: value["caseId"],
                    documentName: value["title"],
                };
                window.external.SaveAsOpenEsdh(JSON.stringify(metadata), JSON.stringify(metadata));
            },

            _onCancel: function() {
                window.external.CancelOpenEsdh();
            },

            onFindCase: function() {
            	this.alfPublish(this.FindCaseDialogTopic);
            },

            onCreateCase: function() {
                this.alfPublish("ALF_CREATE_DIALOG_REQUEST", {
                    contentWidth: "800px",
                    dialogTitle: "Create Case",
                    handleOverflow: false,
                    hideTopic: "GRID_ROW_SELECTED",
                    widgetsContent: [
                        {
                            name: 'alfresco/layout/VerticalWidgets',
                            config: {
                                widgets: [
                                    {
                                        name: "alfresco/forms/ControlRow",
                                        config: {
                                            description: "",
                                            title: "",
                                            fieldId: "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                                            widgets: [
                                                {
                                                    name: "alfresco/forms/controls/HiddenValue",
                                                    config: {
                                                        fieldId: "edb22ed0-ch9a-48f4-8f30-c5atjd748ffb",
                                                        name: "alf_destination",
//                                                        value: caseContainerNodeRef,
                                                        label: "",
                                                        unitsLabel: "",
                                                        description: "",
                                                        postWhenHiddenOrDisabled: true,
                                                        noValueUpdateWhenHiddenOrDisabled: false,
                                                        validationConfig: {
                                                            regex: ".*"
                                                        },
                                                        placeHolder: "",
                                                        widgets: []
                                                    },
                                                    widthPc: "1"
                                                },
                                                {
                                                    name: "alfresco/forms/controls/HiddenValue",
                                                    config: {
                                                        fieldId: "edb31ed0-c74a-48f4-8f30-c5atbd748ffb",
                                                        value: "",
                                                        label: "",
                                                        unitsLabel: "",
                                                        description: "",
                                                        postWhenHiddenOrDisabled: true,
                                                        noValueUpdateWhenHiddenOrDisabled: false,
                                                        validationConfig: {
                                                            regex: ".*"
                                                        },
                                                        placeHolder: "",
                                                        widgets: []
                                                    },
                                                    widthPc: "1"
                                                }
                                            ]
                                        }
                                    },
                                    {
                                        name: "alfresco/forms/ControlRow",
                                        config: {
                                            description: "",
                                            title: "",
                                            fieldId: "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                                            widgets: [
                                                {
                                                    name: "alfresco/forms/controls/DojoValidationTextBox",
                                                    config: {
                                                        fieldId: "edb19ed0-c74a-48f4-8f30-c5aabd74fffb",
                                                        name: "prop_cm_title",
                                                        value: "",
                                                        label: "Title",
                                                        unitsLabel: "",
                                                        description: "The name of the case (Note: This will be the name of the document record)",
                                                        visibilityConfig: {
                                                            initialValue: true,
                                                            rules: []
                                                        },
                                                        requirementConfig: {
                                                            initialValue: false,
                                                            rules: []
                                                        },
                                                        disablementConfig: {
                                                            initialValue: false,
                                                            rules: []
                                                        },
                                                        postWhenHiddenOrDisabled: true,
                                                        noValueUpdateWhenHiddenOrDisabled: false,
                                                        validationConfig: {
                                                            regex: ".*"
                                                        },
                                                        placeHolder: "",
                                                        widgets: []
                                                    },
                                                    widthPc: "98"
                                                }
                                            ]
                                        }
                                    },
                                    {
                                        name: "alfresco/forms/ControlRow",
                                        config: {
                                            description: "",
                                            title: "",
                                            fieldId: "88ba8d88-b562-4954-81b9-d34ac564d5ff",
                                            widgets: [
                                                {

                                                    name: "openesdh/common/widgets/controls/WrappedYUIAuthorityWidget",
                                                    config: {
                                                        id: "create_case_owner_widget",
                                                        name: "assoc_case_owners_added",
                                                        visibilityConfig: { initialValue: true },
                                                        fieldId: "b03a9abe-c4be-44aa-8e1f-c1048172eba1",
                                                        multiple: true,
                                                        label: "Owner(s)"
                                                    }
                                                },

                                                {
                                                    name: "alfresco/forms/controls/DojoSelect",
                                                    config: {
                                                        id: "prop_oe_status",
                                                        label: "Status",
                                                        optionsConfig: {
                                                            fixed: [{label: "Planned"}, {label: "In progress"}, {label: "Existing"}, {label: "Pending"}, {label: "Archived"}, {label: "Cancelled"}, {label: "Closed"}]
                                                        },
                                                        unitsLabel: "",
                                                        description: "",
                                                        name: "prop_oe_status",
                                                        fieldId: "ebf9b987-9744-47ad-8823-ee9d9aa783f4",
                                                        widgets: []
                                                    }
                                                }
                                            ]
                                        }
                                    },
                                    {
                                        name: "alfresco/forms/ControlRow",
                                        config: {
                                            description: "",
                                            title: "",
                                            fieldId: "b0632dac-002e-4860-884b-b9237246075c",
                                            widgets: [
                                                {
                                                    name: "openesdh/common/widgets/controls/DojoDateExt",
                                                    config: {
                                                        id: "prop_case_startDate",
                                                        unitsLabel: "mm/dd/yy",
                                                        description: "Starting date of the case",
                                                        label: "Start date",
                                                        name: "prop_case_startDate",
                                                        fieldId: "b4bd606f-66ae-4f06-847d-dfdc77f5abc2",
                                                        widgets: []
                                                    }
                                                },
                                                {
                                                    name: "openesdh/common/widgets/controls/DojoDateExt",
                                                    config: {
                                                        id: "prop_case_endDate",
                                                        unitsLabel: "mm/dd/yy",
                                                        description: "The end date of the case",
                                                        label: "End date",
                                                        name: "prop_case_endDate",
                                                        fieldId: "69707d94-0f8c-4966-832a-a1adbc53b74f",
                                                        widgets: []
                                                    }
                                                }
                                            ]
                                        }
                                    },
                                    {
                                        name: "alfresco/forms/ControlRow",
                                        config: {
                                            description: "",
                                            title: "",
                                            fieldId: "0b4ab71a-26ce-4df9-839f-c26b12fffecb",
                                            widgets: [
                                                {
                                                    name: "alfresco/forms/controls/DojoTextarea",
                                                    config: {
                                                        fieldId: "63854d9e-295a-454d-8c0d-685de6f68d71",
                                                        name: "prop_cm_description",
                                                        value: "",
                                                        label: "Description",
                                                        unitsLabel: "",
                                                        description: "Description",
                                                        visibilityConfig: {
                                                            initialValue: true,
                                                            rules: []
                                                        },
                                                        requirementConfig: {
                                                            initialValue: false,
                                                            rules: []
                                                        },
                                                        disablementConfig: {
                                                            initialValue: false,
                                                            rules: []
                                                        },
                                                        postWhenHiddenOrDisabled: true,
                                                        noValueUpdateWhenHiddenOrDisabled: false,
                                                        widgets: []
                                                    },
                                                    widthPc: "98"
                                                }
                                            ]
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
                                label: "Create",
                                publishTopic: "NO_OP"
                            }
                        },
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