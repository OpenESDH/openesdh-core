define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "alfresco/core/NodeUtils",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin",
        "alfresco/services/_NavigationServiceTopicMixin",
        "alfresco/dialogs/AlfFormDialog",
        "alfresco/core/NotificationUtils",
        "openesdh/common/widgets/controls/category/CategoryPickerControl"],
    function (declare, AlfCore, CoreXhr, NodeUtils, array, lang, _TopicsMixin, _NavigationServiceTopicMixin, AlfFormDialog, NotificationUtils, CategoryPickerControl) {

        return declare([AlfCore, CoreXhr, _TopicsMixin, NotificationUtils], {

            i18nRequirements: [
                {i18nFile: "./i18n/CaseService.properties"}
            ],

            /**
             * The nodeRef of the cases container.
             */
            casesFolderNodeRef: null,

            /**
             * This is meant to be an array of case status types for the create case dialog select control
             */
            caseConstraintsList: null,

            /**
             * The nodeRef for the case
             */
            caseNodeRef: "",

            /**
             * The case id for the case.
             */
            caseId: "",

            /**
             * The authenticated user (Initially used for the Authority Picker)
             */
            currentUser:null,

            constructor: function (args) {
                lang.mixin(this, args);
                this.caseId = args.caseId;
                this.caseNodeRef = args.nodeRef;

                // Load the case info
                this._caseInfo(this.caseNodeRef, lang.hitch(this, "_onCaseInfoInitialLoadSuccess"));

                this.alfSubscribe("OPENESDH_JOURNALIZE", lang.hitch(this, "onJournalize"));
                this.alfSubscribe("OPENESDH_UNJOURNALIZE", lang.hitch(this, "onUnJournalize"));
                this.alfSubscribe("ALF_WIDGETS_READY", lang.hitch(this, "onAllWidgetsReady"));
                this.alfSubscribe(this.CreateCaseTopic, lang.hitch(this, "_onCreateCaseTopic"));
                this.alfSubscribe(this.ShowCreateCaseDialog, lang.hitch(this, "_showCreateCaseDialog"));
                this.alfSubscribe(this.CreateCaseSuccess, lang.hitch(this, "_onCreateCaseTopicSuccess"));

                this._getLoggedInUser();
                this._getCaseConstraints();//Get the lis of all case constraints

                // Don't do anything when the widgets are ready
                // This is overwritten when the case info is loaded
                this._allWidgetsProcessedFunction = function () {};
            },

            _showCreateCaseDialog: function dk_openesdh__showCreateCaseDialog(payload) {
                var publishOnSuccessTopic = (payload.publishOnSuccessTopic != null ? payload.publishOnSuccessTopic : this.CreateCaseSuccess);
                this.createCaseDialog = new AlfFormDialog({
                    dialogTitle: this.message("create-case.dialog.title"),
                    dialogConfirmationButtonTitle: this.message("create-case.label.button.create"),
                    dialogCancellationButtonTitle: this.message("create-case.label.button.cancel"),
                    formSubmissionTopic: this.CreateCaseTopic,
                    formSubmissionPayload: {
                        publishOnSuccessTopic: publishOnSuccessTopic
                    },
                    widgets: this._getCreateCaseWidgets(payload.caseType)
                });
                this.createCaseDialog.show();
            },

            _onCreateCaseTopic: function (payload){
                var url = Alfresco.constants.PROXY_URI + "api/type/case%3Asimple/formprocessor";

                //Convert the owners array to string, in the case of more than one owner
                payload.assoc_case_owners_added = payload.assoc_case_owners_added.toString();
                console.log("Payload converted to string: "+ payload.assoc_case_owners_added);

                this.serviceXhr({
                    url: url,
                    method: "POST",
                    data: payload,
                    successCallback:function (response, config) {
                        this.alfPublish(payload.publishOnSuccessTopic, response)
                    },
                    callbackScope: this});
            },

            _onCreateCaseTopicSuccess: function (payload){
                console.log("CaseService(66) navigating to dashboard");
                // construct the url to call
                var persistedObjNodeRef = NodeUtils.processNodeRef(payload.persistedObject);
                var url = Alfresco.constants.PROXY_URI + "api/openesdh/documents/isCaseDoc/" + persistedObjNodeRef.uri;

                this.serviceXhr({
                        url: url,
                        method: "GET",
                        successCallback: function (response, config) {
                            this.alfPublish("ALF_NAVIGATE_TO_PAGE", {
                                type: "CONTEXT_RELATIVE",
                                target: "CURRENT",
                                url: "page/oe/case/" + response.caseId + "/dashboard"
                            }
                        )
                    },
                    callbackScope: this});
            },

            _caseInfo: function (nodeRefOrCaseId, successCallback) {

                // Get caseInfo from webscript
                if (nodeRefOrCaseId == null) {
                    this.alfLog("error", "Null nodeRef or caseId passed to _caseInfo");
                    return false;
                }
                var url;
                if (nodeRefOrCaseId.indexOf("://") === -1) {
                    url = Alfresco.constants.PROXY_URI + "api/openesdh/caseinfo/" + nodeRefOrCaseId;
                } else {
                    url = Alfresco.constants.PROXY_URI + "api/openesdh/caseinfo?nodeRef=" + nodeRefOrCaseId;
                }

                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: successCallback,
                    callbackScope: this});
            },

            onAllWidgetsReady: function (payload) {
                this._allWidgetsProcessedFunction();
            },

            _onCaseInfoInitialLoadSuccess: function (response, config) {
                this._allWidgetsProcessedFunction = lang.hitch(this, function () {
                    this.alfPublish(this.CaseInfoTopic, response);
                    this.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: response.allProps.properties["cm:title"].value});
                });
                // Call it immediately after we receive the response, and let
                // it be called each time we get an ALF_WIDGETS_READY.
                this._allWidgetsProcessedFunction();
            },

            onJournalize: function (payload) {
                // Journalize the nodeRef in the payload if provided, otherwise the current case on the page
                var nodeRef = payload.nodeRef != null ? payload.nodeRef : this.caseNodeRef;
                if (nodeRef == null) {
                    this.alfLog("error", "No 'nodeRef' was provided to journalize and we are not on a case page.");
                    return;
                }
                var isCaseDoc = payload.isCaseDoc != null ? payload.isCaseDoc : false;
                var msgPrefix = "journalize." + (payload.isCaseDoc ? "doc" : "case");

                var dialog = new AlfFormDialog({
                    pubSubScope: this.pubSubScope,
                    title: this.message(msgPrefix + ".dialog.title"),
                    formSubmissionTopic: "JOURNALIZE_DIALOG_OK",
                    formSubmissionPayload: {
                        nodeRef: nodeRef,
                        isCaseDoc: isCaseDoc
                    },
                    dialogConfirmationButtonTitle: this.message("button.ok"),
                    dialogCancellationButtonTitle: this.message("button.cancel"),
                    widgets: [
                        {
                            name: "openesdh/common/widgets/controls/category/CategoryPickerControl",
                            config: {
                                name: "journalKey",
                                label: this.message("journal.key"),
                                requirementConfig: {
                                    initialValue: true
                                }
                                // TODO: Set the root category for the journal key
//                                rootNodeRef: "workspace://SpacesStore/abc/"
                            }
                        }
                    ]
                });
                dialog.show();

                this.alfSubscribe("JOURNALIZE_DIALOG_OK", lang.hitch(this, "_onJournalizeDialogOK"));
            },

            _onJournalizeDialogOK: function (payload) {
                var msgPrefix = "journalize." + (payload.isCaseDoc ? "doc" : "case");
                if (!confirm(this.message(msgPrefix + ".confirm"))) {
                    return false;
                }

                var journalKey;
                // Get the one key from the category picker widget which contains
                // the nodeRef of the journalKey
                for (var property in payload.journalKey) {
                    if (!payload.journalKey.hasOwnProperty(property)) continue;
                    journalKey = property;
                }

                var nodeRef = payload.nodeRef;

                var url = Alfresco.constants.PROXY_URI + "api/openesdh/" + NodeUtils.processNodeRef(nodeRef).uri + "/journalize?journalKey=" + journalKey;
                this.serviceXhr({
                    url: url,
                    method: "PUT",
                    data: {},
                    successCallback: lang.hitch(this, function () {
                        this.displayMessage(this.message(msgPrefix + '.success'));
                        setTimeout(lang.hitch(window.location, 'reload'), 500);
                    }),
                    failureCallback: lang.hitch(this, function () {
                        this.displayMessage(this.message(msgPrefix + '.failure'));
                        setTimeout(lang.hitch(window.location, 'reload'), 500);
                    }),
                    callbackScope: this
                });
            },

            onUnJournalize: function (payload) {
                // Journalize the nodeRef in the payload if provided, otherwise the current case on the page
                var nodeRef = payload.nodeRef != null ? payload.nodeRef : this.caseNodeRef;
                if (nodeRef == null) {
                    this.alfLog("error", "No 'nodeRef' was provided to unjournalize and we are not on a case page.");
                    return;
                }

                var isCaseDoc = payload.isCaseDoc != null ? payload.isCaseDoc : false;
                var msgPrefix = "unjournalize." + (isCaseDoc ? "doc" : "case");
                if (confirm(this.message(msgPrefix + ".confirm"))) {
                    var url = Alfresco.constants.PROXY_URI + "api/openesdh/" + NodeUtils.processNodeRef(nodeRef).uri + "/journalize?unjournalize=true";
                    this.serviceXhr({
                        url: url,
                        method: "PUT",
                        data: {},
                        successCallback: lang.hitch(this, function () {
                            this.displayMessage(this.message(msgPrefix + '.success'));
                            setTimeout(lang.hitch(window.location, 'reload'), 500);
                        }),
                        failureCallback: lang.hitch(this, function () {
                            this.displayMessage(this.message(msgPrefix + '.failure'));
                            setTimeout(lang.hitch(window.location, 'reload'), 500);
                        }),
                        callbackScope: this
                    });
                }
            },

            //Internally used functions

            /**
             * Constructs the options for the select controls for the case creation dialog
             */
            _getSelectControlOptions: function dk_openesdh_setCaseConstraintLists(listName) {
                var options = [];
                var states = this.caseConstraintsList[listName];

                for (var state in states) {
                    options.push({
                        value:  states[state].value,
                        label: states[state].label
                    });
                }
                return options;
            },

            _getCaseConstraints: function dk_openesdh___getCaseConstraints() {
                var url =  Alfresco.constants.PROXY_URI + "api/openesdh/case/constraints";
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: (function (payload) {
                        this.caseConstraintsList =  payload;
                    }),
                    callbackScope: this});
            },

            _getCreateCaseWidgets: function dk_openesdh__getCreateCaseWidgets(caseType){
                var caseContainerNodeRef = this.casesFolderNodeRef;

                switch(caseType){
                    case "simple" :
                        return [
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
                                                value: caseContainerNodeRef,
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
                                                label: this.message("create-case.label.title"),
                                                unitsLabel: "",
                                                description: "",
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
                                            widthPc: "70"
                                        },
                                        {
                                            name: "openesdh/common/widgets/controls/Select",
                                            config: {
                                                id: "prop_oe_status",
                                                label: this.message("create-case.label.button.case-status"),
                                                optionsConfig: {
                                                    fixed: this._getSelectControlOptions("simpleStatusConstraint")
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
                                    fieldId: "88ba8d88-b562-4954-81b9-d34ac564d5ff",
                                    widgets: [
                                        {
                                            name: "openesdh/common/widgets/controls/AuthorityPicker",
                                            id:"create_case_dialog_auth_picker",
                                            config: {
                                                label: "Owner",
                                                name: "assoc_case_owners_added",
                                                itemKey: "nodeRef",
                                                singleItemMode: false,
                                                setDefaultPickedItems: true,
                                                defaultPickedItems: this.currentUser,
                                                /**
                                                 *Override widgetsForControl to remove the "remove all" button
                                                 */
                                                widgetsForControl: [
                                                    {
                                                        name: "alfresco/layout/VerticalWidgets",
                                                        assignTo: "verticalWidgets",
                                                        config: {
                                                            widgets: [
                                                                {
                                                                    name: "alfresco/pickers/PickedItems",
                                                                    assignTo: "pickedItemsWidget",
                                                                    config: {
                                                                        pubSubScope: "{itemSelectionPubSubScope}"
                                                                    }
                                                                },
                                                                {
                                                                    name: "alfresco/buttons/AlfButton",
                                                                    id: "create_case_dialog_auth_picker_button",
                                                                    assignTo: "formDialogButton",
                                                                    config: {
                                                                        label: "picker.add.label",
                                                                        publishTopic: "ALF_CREATE_DIALOG_REQUEST",
                                                                        publishPayload: {
                                                                            dialogTitle: "picker.select.title",
                                                                            handleOverflow: false,
                                                                            widgetsContent: [
                                                                                {
                                                                                    name: "{pickerWidget}",
                                                                                    config: {}
                                                                                }
                                                                            ],
                                                                            widgetsButtons: [
                                                                                {
                                                                                    name: "alfresco/buttons/AlfButton",
                                                                                    id: "create_case_dialog_auth_picker_picked_ok_button",
                                                                                    config: {
                                                                                        label: "picker.ok.label",
                                                                                        publishTopic: "ALF_ITEMS_SELECTED",
                                                                                        pubSubScope: "{itemSelectionPubSubScope}"
                                                                                    }
                                                                                },
                                                                                {
                                                                                    name: "alfresco/buttons/AlfButton",
                                                                                    id:"create_case_dialog_auth_picker_picked_cancel_button",
                                                                                    config: {
                                                                                        label: "picker.cancel.label",
                                                                                        publishTopic: "NO_OP"
                                                                                    }
                                                                                }
                                                                            ]
                                                                        },
                                                                        publishGlobal: true
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
                                                description: "",
                                                label: this.message("create-case.label.button.start-date"),
                                                name: "prop_case_startDate",
                                                fieldId: "b4bd606f-66ae-4f06-847d-dfdc77f5abc2",
                                                value: new Date()
                                            }
                                        },
                                        {
                                            name: "openesdh/common/widgets/controls/DojoDateExt",
                                            config: {
                                                id: "prop_case_endDate",
                                                unitsLabel: "mm/dd/yy",
                                                description: "",
                                                label: this.message("create-case.label.button.end-date"),
                                                name: "prop_case_endDate",
                                                fieldId: "69707d94-0f8c-4966-832a-a1adbc53b74f",
                                                value: new Date()
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
                                                description: "",
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
                        ];
                    case "complaint" : //This one is for testing purposes only
                        return [
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
                                                value: caseContainerNodeRef,
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
                                                label: this.message("create-case.label.title"),
                                                unitsLabel: "",
                                                description: "",
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
                                            widthPc: "70"
                                        },
                                        {
                                            name: "openesdh/common/widgets/controls/Select",
                                            config: {
                                                id: "prop_oe_status",
                                                label: this.message("create-case.label.button.case-status"),
                                                optionsConfig: {
                                                    fixed: this._getSelectControlOptions("complaintStatusConstraint")
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
                                    fieldId: "88ba8d88-b562-4954-81b9-d34ac564d5ff",
                                    widgets: [
                                        {
                                            name: "alfresco/forms/ControlRow",
                                            config: {
                                                description: "",
                                                title: "",
                                                fieldId: "31ed6de4-3a60-46bb-83e9-40b04ae0dd37",
                                                widgets: [
                                                    {
                                                        name: "openesdh/common/widgets/controls/AuthorityPicker",
                                                        id:"create_case_dialog_auth_picker",
                                                        config: {
                                                            label: "Owner",
                                                            name: "assoc_case_owners_added",
                                                            itemKey: "nodeRef",
                                                            singleItemMode: false,
                                                            setDefaultPickedItems: true,
                                                            defaultPickedItems: this.currentUser,
                                                            /**
                                                             *Override widgetsForControl to remove the "remove all" button
                                                             */
                                                            widgetsForControl: [
                                                                {
                                                                    name: "alfresco/layout/VerticalWidgets",
                                                                    assignTo: "verticalWidgets",
                                                                    config: {
                                                                        widgets: [
                                                                            {
                                                                                name: "alfresco/pickers/PickedItems",
                                                                                assignTo: "pickedItemsWidget",
                                                                                config: {
                                                                                    pubSubScope: "{itemSelectionPubSubScope}"
                                                                                }
                                                                            },
                                                                            {
                                                                                name: "alfresco/buttons/AlfButton",
                                                                                id: "create_case_dialog_auth_picker_button",
                                                                                assignTo: "formDialogButton",
                                                                                config: {
                                                                                    label: "picker.add.label",
                                                                                    publishTopic: "ALF_CREATE_DIALOG_REQUEST",
                                                                                    publishPayload: {
                                                                                        dialogTitle: "picker.select.title",
                                                                                        handleOverflow: false,
                                                                                        widgetsContent: [
                                                                                            {
                                                                                                name: "{pickerWidget}",
                                                                                                config: {}
                                                                                            }
                                                                                        ],
                                                                                        widgetsButtons: [
                                                                                            {
                                                                                                name: "alfresco/buttons/AlfButton",
                                                                                                id: "create_case_dialog_auth_picker_picked_ok_button",
                                                                                                config: {
                                                                                                    label: "picker.ok.label",
                                                                                                    publishTopic: "ALF_ITEMS_SELECTED",
                                                                                                    pubSubScope: "{itemSelectionPubSubScope}"
                                                                                                }
                                                                                            },
                                                                                            {
                                                                                                name: "alfresco/buttons/AlfButton",
                                                                                                id:"create_case_dialog_auth_picker_picked_cancel_button",
                                                                                                config: {
                                                                                                    label: "picker.cancel.label",
                                                                                                    publishTopic: "NO_OP"
                                                                                                }
                                                                                            }
                                                                                        ]
                                                                                    },
                                                                                    publishGlobal: true
                                                                                }
                                                                            }
                                                                        ]
                                                                    }
                                                                }
                                                            ]
                                                        }
                                                    },
                                                    {
                                                        name: "alfresco/forms/controls/DojoValidationTextBox",
                                                        config: {
                                                            fieldId: "edba6ed0-c74a-48f4-9c30-c5aabd74ff1b",
                                                            name: "prop_case_subject",
                                                            value: "",
                                                            label: this.message("create-case.label.subject"),
                                                            unitsLabel: "",
                                                            description: "",
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
                                                        widthPc: "50"
                                                    }
                                                ]
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
                                                description: "",
                                                label: this.message("create-case.label.button.start-date"),
                                                name: "prop_case_startDate",
                                                fieldId: "b4bd606f-66ae-4f06-847d-dfdc77f5abc2",
                                                value: new Date()
                                            }
                                        },
                                        {
                                            name: "openesdh/common/widgets/controls/DojoDateExt",
                                            config: {
                                                id: "prop_case_endDate",
                                                unitsLabel: "mm/dd/yy",
                                                description: "",
                                                label: this.message("create-case.label.button.end-date"),
                                                name: "prop_case_endDate",
                                                fieldId: "69707d94-0f8c-4966-832a-a1adbc53b74f",
                                                value: new Date()
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
                                                description: "",
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
                        ];
                }
        },

            _getLoggedInUser: function dk_openesdh__getLoggedInUser(){
                var url =  Alfresco.constants.PROXY_URI + "/api/openesdh/currentUser";
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: function (response) {
                        this.currentUser = response;
                    },
                    callbackScope: this});
            }

        });
    });