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
        "openesdh/extensions/core/ObjectProcessingMixin",
        "openesdh/common/widgets/controls/category/CategoryPickerControl"],
    function (declare, AlfCore, CoreXhr, NodeUtils, array, lang, _TopicsMixin, _NavigationServiceTopicMixin,
              AlfFormDialog, NotificationUtils, ObjectProcessingMixin, CategoryPickerControl) {

        return declare([AlfCore, CoreXhr, _TopicsMixin, NotificationUtils, ObjectProcessingMixin], {

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
             * This is meant to be an array of select options containing workflows specific to cases
             */
            caseWorkflowList: null,

            /**
             * The nodeRef for a case
             */
            caseNodeRef: "",

            /**
             * The case id for a case.
             */
            caseId: "",

            /**
             * This should be an object containing case types and their corresponding array of widgets required to
             * create the cases.
             * { caseType: [] }
             */
            createCaseWidgets:null,

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

                // Don't do anything when the widgets are ready
                // This is overwritten when the case info is loaded
                this._allWidgetsProcessedFunction = function () {};

                var _widgets = lang.clone(this.createCaseWidgets);
                this.processObject([ "processInstanceTokens"], _widgets);

                this.createCaseWidgets = _widgets;
            },

            _showCreateCaseDialog: function dk_openesdh__showCreateCaseDialog(payload) {
                var publishOnSuccessTopic = (payload.publishOnSuccessTopic != null ? payload.publishOnSuccessTopic : this.CreateCaseSuccess);
                this.alfPublish("ALF_CREATE_FORM_DIALOG_REQUEST",
                    {
                        dialogId:"CREATE_CASE_DIALOG",
                        dialogTitle: this.message("create-case.dialog.title"),
                        dialogConfirmationButtonTitle: this.message("create-case.label.button.create"),
                        dialogCancellationButtonTitle: this.message("create-case.label.button.cancel"),
                        formSubmissionTopic: this.CreateCaseTopic,
                        formSubmissionPayloadMixin: {
                            publishOnSuccessTopic: publishOnSuccessTopic
                        },
                        fixedWidth: true,
                        widgets: this._getCreateCaseWidgets(payload.caseType)
                    });
            },

            _onCreateCaseTopic: function (payload){
                var url = Alfresco.constants.PROXY_URI + "api/type/"+payload.caseType+"%3Acase/formprocessor";
                //Convert the owners array to string, in the case of more than one owner
                payload.assoc_base_owners_added = payload.assoc_base_owners_added.toString();

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

                    var caseProps  = response.allProps.properties;
                    var caseTitle = caseProps["cm:name"].value + "      " + caseProps["cm:title"].value;
                    this.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: caseTitle});
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
                                },
                                // TODO: Set the root category for the journal key
                                initialPath: "kle_emneplan"
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

                var journalKey = payload.journalKey;
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
             * Constructs the options for the select controls for the case workflow select control
             * and publishes it.
             */
            _getWorkFlowSelectControlOptions: function dk_openesdh_setCaseConstraintLists(payload) {
                var options = [];
                var states = this.caseWorkflowList;

                options.push({
                    label: "Please Select a Workflow",
                    value: "",
                    selected: true
                });

                console.log("CaseService (294) publishing select control options....");

                for (var state in states) {
                    options.push({
                        value:  states[state].name,
                        label: states[state].title
                    });
                }

                this.alfPublish(payload.responseTopic, {options:options} );
            },

            _getCaseWorkflows: function dk_openesdh___getCaseWorkflows() {
                var url =  Alfresco.constants.PROXY_URI + "api/openesdh/case/workflow/definitions";
                this.serviceXhr({
                    url: url,
                    method: "GET",
                    successCallback: (function (payload) {
                        this.caseWorkflowList =  payload.data;
                    }),
                    callbackScope: this});
            },

            _getCreateCaseWidgets: function dk_openesdh__getCreateCaseWidgets(caseType){
                return this.createCaseWidgets[caseType];
            }


        });
    });