define([
    "dojo/_base/declare",
    "alfresco/forms/controls/BaseFormControl",
    "alfresco/core/CoreWidgetProcessing",
    "alfresco/core/ObjectProcessingMixin",
    "dojo/_base/lang"],
    function(declare, BaseFormControl, CoreWidgetProcessing, ObjectProcessingMixin, lang) {
        return declare([BaseFormControl, CoreWidgetProcessing, ObjectProcessingMixin], {

            cssRequirements: [{cssFile: "./css/CasePicker.css"}],

            i18nRequirements: [{i18nFile: "./i18n/CasePicker.properties"}],

            getWidgetConfig: function() {
                return {
                    id: this.id + "_CONTROL",
                    name: this.name,
                    value: this.value
                };
            },

            createFormControl: function(config) {
                wrappedWidget = lang.getObject("verticalWidgets.pickedCaseWidget", false, this);
                this.itemSelectionPubSubScope = this.generateUuid();
                this.alfSubscribe(this.itemSelectionPubSubScope + "ALF_CASE_SELECTED", lang.hitch(this, "onCaseSelected"), true);
                this.alfSubscribe("GRID_ROW_SELECTED", lang.hitch(this, "onCaseSelected"), true);
                this.alfSubscribe("ALF_SET_SEARCH_TERM", lang.hitch(this, this.onSearchTermRequest));

                var clonedWidgetsForControl = lang.clone(this.widgetsForControl);
                this.processObject(["processInstanceTokens"], clonedWidgetsForControl);
                return this.processWidgets(clonedWidgetsForControl, this._controlNode);
            },

            onCaseSelected: function(payload) {
                var row = payload.row;

//                alert(JSON.stringify(row.data["oe:caseId"]));
//                alert("onCaseSelected: " + payload.row.data.oe:id);
                var pickedCaseWidget = this.getPickedCaseWidget();
                if (pickedCaseWidget != null && payload != null) {
                    pickedCaseWidget.setValue(row.data["oe:caseId"]);
                }
//                this.alfPublish("ALF_CLOSE_DIALOG");
            },

            onSearchTermRequest: function() {
            },

            getValue: function() {
                return this.getPickedCaseWidget().getValue();
            },

            getPickedCaseWidget: function() {
                return lang.getObject("verticalWidgets.pickedCaseWidget", false, this);
            },

            widgetsForControl: [
                {
                    name: "alfresco/layout/VerticalWidgets",
                    assignTo: "verticalWidgets",
                    config: {
                        widgets: [
                            {
                                name: "alfresco/forms/controls/DojoValidationTextBox",
                                assignTo: "pickedCaseWidget",
                                config: {
                                }
                            },
                            {
                                name: "alfresco/buttons/AlfButton",
                                assignTo: "formDialogButton",
                                config: {
                                    label: "Find Case",
                                    publishTopic: "ALF_CREATE_DIALOG_REQUEST",
                                    publishPayload: {
                                        hideTopic: "GRID_ROW_SELECTED",
                                        dialogTitle: "Find Case",
                                        handleOverflow: false,
                                        widgetsContent: [
                                            {
                                                name: 'alfresco/layout/VerticalWidgets',
                                                config: {
                                                    widgets: [
                                                        {
                                                            name: "openesdh/xsearch/FilterPane",
                                                            config: {
                                                                baseType: "case:simple",
                                                                types: {
                                                                    "case:simple": {
                                                                        name: "case:simple",
                                                                        title: "Simple Case"
                                                                    }
                                                                },
                                                                properties: {
                                                                    "cm:title": {
                                                                        "name": "cm:title",
                                                                        "title": "Title",
                                                                        "description": "",
                                                                        "dataType": "d:mltext",
                                                                        "defaultValue": null,
                                                                        "multiValued": false,
                                                                        "mandatory": true,
                                                                        "enforced": false,
                                                                        "protected": false,
                                                                        "indexed": true,
                                                                        "url": "\/api\/classes\/oe_titled\/property\/cm_title",
                                                                        "constraints": [

                                                                        ],
                                                                        "control": "FilterTextWidget"
                                                                    },
                                                                    "oe:status": {
                                                                        "name": "oe:status",
                                                                        "title": "Status",
                                                                        "description": "",
                                                                        "dataType": "d:text",
                                                                        "defaultValue": null,
                                                                        "multiValued": false,
                                                                        "mandatory": false,
                                                                        "enforced": false,
                                                                        "protected": false,
                                                                        "indexed": true,
                                                                        "url": "/api/classes/case_simple/property/oe_status",
                                                                        control: "FilterSelectWidget"
                                                                    },
                                                                    "TYPE": {
                                                                        "name": "TYPE",
                                                                        "title": "Type",
                                                                        "dataType": "d:text",
                                                                        "control": "FilterSelectWidget",
                                                                        "options": [
                                                                            {
                                                                                "label": "Simple Case",
                                                                                "value": "case:simple"
                                                                            }
                                                                        ]
                                                                    },
                                                                    "ALL": {
                                                                        "name": "ALL",
                                                                        "title": "S\u00f8g",
                                                                        "dataType": "d:text",
                                                                        "control": "FilterTextWidget"
                                                                    }
                                                                },
                                                                "availableFilters": [
                                                                    "ALL",
                                                                    "TYPE",
//                                                                    "oe:id",
                                                                    "cm:title",
//                                                                    "cm:modified",
                                                                    "oe:status"
                                                                ],
                                                                "operatorSets": {
                                                                    "equality": [
                                                                        {
                                                                            "name": "er",
                                                                            "value": "="
                                                                        },
                                                                        {
                                                                            "name": "er ikke",
                                                                            "value": "!="
                                                                        }
                                                                    ]
                                                                }
                                                            }
                                                        },
                                                        {
                                                            name: "openesdh/xsearch/Grid",
//                                                            config: {
//                                                                baseType: baseType,
//                                                                types: searchModel.types,
//                                                                properties: searchModel.properties,
//                                                                visibleColumns: searchDefinition.visibleColumns,
//                                                                availableColumns: searchDefinition.availableColumns,
//                                                                actions: searchDefinition.actions
//                                                            }
//                                                        }
//                                                        {
//                                                            name: "openesdh/pages/case/widgets/MyCasesWidget",
                                                            config: {
                                                                showPagination: true,
                                                                getColumns: function () {
                                                                    return [
                                                                        {
                                                                            field: "oe:id",
                                                                            label: this.message("casepicker.id.label")
                                                                        },
                                                                        {
                                                                            field: "cm:title",
                                                                            label: this.message("casepicker.title.label")
                                                                        },
                                                                        {
                                                                            field: "oe:status",
                                                                            label: this.message("casepicker.state.label")
                                                                        },
                                                                        {
                                                                            field: "cm:modified",
                                                                            label: this.message("casepicker.modified.label"),
                                                                            formatter: lang.hitch(this, "_formatDate")
                                                                        }
                                                                    ];
                                                                },
                                                                actions: [],
                                                                baseType: "case:simple",
                                                                types: {
                                                                    "case:simple": {
                                                                        name: "case:simple",
                                                                        title: "Simple Case"
                                                                    }
                                                                },
                                                                properties: {
                                                                    "cm:title": {
                                                                        "name": "cm:title",
                                                                        "title": "Title",
                                                                        "description": "",
                                                                        "dataType": "d:mltext",
                                                                        "defaultValue": null,
                                                                        "multiValued": false,
                                                                        "mandatory": true,
                                                                        "enforced": false,
                                                                        "protected": false,
                                                                        "indexed": true,
                                                                        "url": "/api/classes/oe_titled/property/cm_title",
                                                                        control: "FilterTextWidget"
                                                                    },
                                                                    "oe:status": {
                                                                        "name": "oe:status",
                                                                        "title": "Status",
                                                                        "description": "",
                                                                        "dataType": "d:text",
                                                                        "defaultValue": null,
                                                                        "multiValued": false,
                                                                        "mandatory": false,
                                                                        "enforced": false,
                                                                        "protected": false,
                                                                        "indexed": true,
                                                                        "url": "/api/classes/case_base/property/oe_status",
                                                                        "constraints": [
                                                                            {
                                                                                "type": "LIST",
                                                                                "parameters": [
                                                                                    {
                                                                                        "allowedValues": [
                                                                                            "Planlagt",
                                                                                            "Igangv\u00e6rende",
                                                                                            "G\u00e6ldende",
                                                                                            "Afventer",
                                                                                            "Udg\u00e5et",
                                                                                            "Annulleret",
                                                                                            "Lukket"
                                                                                        ]
                                                                                    },
                                                                                    {
                                                                                        "sorted": false
                                                                                    },
                                                                                    {
                                                                                        "caseSensitive": true
                                                                                    }
                                                                                ]
                                                                            }
                                                                        ],
                                                                        "control": "FilterSelectWidget"
                                                                    }
                                                                },
                                                                visibleColumns: ["oe:id", "cm:title", "oe:status", "cm:modified"]
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
                                    },
                                    publishGlobal: true
                                }
                            },
                            {
                                name: "alfresco/buttons/AlfButton",
                                config: {
                                    label: "Create Case",
                                }
                            }
                        ]
                    }
                }
            ]
        });
    }
);