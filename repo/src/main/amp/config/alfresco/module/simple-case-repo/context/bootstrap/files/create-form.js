{
    "widgets" : [
        {
            "name": "alfresco/forms/ControlRow",
            "config": {
                "description": "",
                "title": "",
                "fieldId": "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                "widgets": [
                    {
                        "name": "alfresco/forms/controls/HiddenValue",
                        "config": {
                            "fieldId": "edb22ed0-ch9a-48f4-8f30-c5atjd748ffb",
                            "name": "alf_destination",
                            "value": "{casesFolderNodeRef}",
                            "label": "",
                            "unitsLabel": "",
                            "description": "",
                            "postWhenHiddenOrDisabled": true,
                            "noValueUpdateWhenHiddenOrDisabled": false,
                            "validationConfig": {
                                "regex": ".*"
                            },
                            "placeHolder": "",
                            "widgets": []
                        },
                        "widthPc": "1"
                    },
                    {
                        "name": "alfresco/forms/controls/HiddenValue",
                        "config": {
                            "fieldId": "edb31ed0-c74a-48f4-8f30-c5atbd748ffb",
                            "name": "caseType",
                            "value": "simple",
                            "label": "",
                            "unitsLabel": "",
                            "description": "",
                            "postWhenHiddenOrDisabled": true,
                            "noValueUpdateWhenHiddenOrDisabled": false,
                            "validationConfig": {
                                "regex": ".*"
                            },
                            "placeHolder": "",
                            "widgets": []
                        },
                        "widthPc": "1"
                    }
                ]
            }
        },
        {
            "name": "alfresco/forms/ControlRow",
            "config": {
                "description": "",
                "title": "",
                "fieldId": "33ed6de4-3a60-46bb-8389-40b04aeddd37",
                "widgets": [
                    {
                        "name": "alfresco/forms/controls/DojoValidationTextBox",
                        "config": {
                            "id":"CREATE_CASE_DIALOG_TITLE_INPUT_FIELD",
                            "fieldId": "edb19ed0-c74a-48f4-8f30-c5aabd74fffb",
                            "name": "prop_cm_title",
                            "value": "",
                            "label": "Case Title/Name",
                            "unitsLabel": "",
                            "description": "",
                            "visibilityConfig": {
                                "initialValue": true,
                                "rules": []
                            },
                            "requirementConfig": {
                                "initialValue": false,
                                "rules": []
                            },
                            "disablementConfig": {
                                "initialValue": false,
                                "rules": []
                            },
                            "postWhenHiddenOrDisabled": true,
                            "noValueUpdateWhenHiddenOrDisabled": false,
                            "validationConfig": {
                                "regex": ".*"
                            },
                            "placeHolder": "",
                            "widgets": []
                        },
                        "widthPc": "70"
                    },
                    {
                        "name": "openesdh/common/widgets/controls/Select",
                        "config": {
                            "id": "CREATE_CASE_DIALOG_STATUS_SELECT_CONTROL",
                            "label": "Case Status",
                            "optionsConfig": {
                                "fixed": "{caseConstraintsList.simple.caseStatusConstraint}"
                            },
                            "unitsLabel": "",
                            "description": "",
                            "name": "prop_oe_status",
                            "fieldId": "ebf9b987-9744-47ad-8823-ee9d9aa783f4",
                            "widgets": []
                        }
                    }
                ]
            }
        },
        {
            "name": "alfresco/forms/ControlRow",
            "config": {
                "description": "",
                "title": "",
                "fieldId": "88ba8d88-b562-4954-81b9-d34ac564d5ff",
                "widgets": [
                    {
                        "name": "openesdh/common/widgets/controls/AuthorityPicker",
                        "id": "CREATE_CASE_DIALOG_AUTH_PICKER",
                        "config": {
                            "label": "create-case.label.button.case-owner",
                            "name": "assoc_base_owners_added",
                            "itemKey": "nodeRef",
                            "currentUser": "{currentUser}",
                            "singleItemMode": false,
                            "setDefaultPickedItems": true,
                            "defaultPickedItems": "{currentUser}",
                            "widgetsForControl": [
                                {
                                    "name": "alfresco/layout/VerticalWidgets",
                                    "assignTo": "verticalWidgets",
                                    "config": {
                                        "widgets": [
                                            {
                                                "name": "alfresco/pickers/PickedItems",
                                                "assignTo": "pickedItemsWidget",
                                                "config": {
                                                    "pubSubScope": "{itemSelectionPubSubScope}"
                                                }
                                            },
                                            {
                                                "name": "alfresco/buttons/AlfButton",
                                                "id": "create_case_dialog_auth_picker_button",
                                                "assignTo": "formDialogButton",
                                                "config": {
                                                    "label": "picker.add.label",
                                                    "publishTopic": "ALF_CREATE_DIALOG_REQUEST",
                                                    "publishPayload": {
                                                        "dialogTitle": "auth-picker.select.title",
                                                        "handleOverflow": false,
                                                        "widgetsContent": [
                                                            {
                                                                "name": "{pickerWidget}",
                                                                "config": {}
                                                            }
                                                        ],
                                                        "widgetsButtons": [
                                                            {
                                                                "name": "alfresco/buttons/AlfButton",
                                                                "id": "create_case_dialog_auth_picker_picked_ok_button",
                                                                "config": {
                                                                    "label": "picker.ok.label",
                                                                    "publishTopic": "ALF_ITEMS_SELECTED",
                                                                    "pubSubScope": "{itemSelectionPubSubScope}"
                                                                }
                                                            },
                                                            {
                                                                "name": "alfresco/buttons/AlfButton",
                                                                "id": "create_case_dialog_auth_picker_picked_cancel_button",
                                                                "config": {
                                                                    "label": "picker.cancel.label",
                                                                    "publishTopic": "NO_OP"
                                                                }
                                                            }
                                                        ]
                                                    },
                                                    "publishGlobal": true
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
        },
        {
            "name": "alfresco/forms/ControlRow",
            "config": {
                "description": "",
                "title": "",
                "fieldId": "b0632dac-002e-4860-884b-b9237246075c",
                "widgets": [
                    {
                        "name": "openesdh/common/widgets/controls/DojoDateExt",
                        "config": {
                            "id": "CREATE_CASE_DIALOG_START_DATE",
                            "unitsLabel": "dd/mm/\u00e5\u00e5\u00e5\u00e5",
                            "description": "",
                            "label": "",
                            "name": "prop_base_startDate",
                            "fieldId": "b4bd606f-66ae-4f06-847d-dfdc77f5abc2",
                            "value": null
                        }
                    },
                    {
                        "name": "openesdh/common/widgets/controls/DojoDateExt",
                        "config": {
                            "id": "prop_base_endDate",
                            "unitsLabel": "dd/mm/\u00e5\u00e5\u00e5\u00e5",
                            "description": "",
                            "label": "",
                            "name": "prop_base_endDate",
                            "fieldId": "69707d94-0f8c-4966-832a-a1adbc53b74f",
                            "value": null
                        }
                    }
                ]
            }
        },
        {
            "name": "alfresco/forms/ControlRow",
            "config": {
                "description": "",
                "title": "",
                "fieldId": "0b4ab71a-26ce-4df9-839f-c26b12fffecb",
                "widgets": [
                    {
                        "name": "alfresco/forms/controls/DojoTextarea",
                        "config": {
                            id:"CREATE_CASE_DIALOG_DESCRIPTION_FIELD",
                            "fieldId": "63854d9e-295a-454d-8c0d-685de6f68d71",
                            "name": "prop_cm_description",
                            "value": "",
                            "label": "create-case.label.description",
                            "unitsLabel": "",
                            "description": "",
                            "visibilityConfig": {
                                "initialValue": true,
                                "rules": []
                            },
                            "requirementConfig": {
                                "initialValue": false,
                                "rules": []
                            },
                            "disablementConfig": {
                                "initialValue": false,
                                "rules": []
                            },
                            "postWhenHiddenOrDisabled": true,
                            "noValueUpdateWhenHiddenOrDisabled": false,
                            "widgets": []
                        },
                        "widthPc": "98"
                    }
                ]
            }
        }
    ]
}