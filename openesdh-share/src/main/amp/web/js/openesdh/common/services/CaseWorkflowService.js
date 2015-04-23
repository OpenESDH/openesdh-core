define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "alfresco/core/NodeUtils",
        "dojo/_base/array",
        "dojo/_base/lang",
        "alfresco/dialogs/AlfFormDialog"],
    function (declare, AlfCore, CoreXhr, NodeUtils, array, lang, AlfFormDialog) {

        return declare([AlfCore, CoreXhr], {

            i18nRequirements: [
                {i18nFile: "./i18n/CaseService.properties"}
            ],

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
             * The authenticated user (Initially used for the Authority Picker)
             */
            currentUser: null,

            /**
             * Topics
             */

            showStartWorkflowDialog: "OE_SHOW_WORKFLOW_DIALOG",
            startWorkflowTopic: "OE_START_CASE_WORKFLOW",
            startWorkflowSuccess: "OE_START_CASE_WORKFLOW_SUCCESS",
            startCaseWorkflowNavigationTopic: "OE_NAVIGATE_WORKFLOW_PAGE_TOPIC",


            constructor: function (args) {
                lang.mixin(this, args);
                this.caseId = args.caseId;
                this.caseNodeRef = args.nodeRef;

                //this.alfSubscribe("ALF_WIDGETS_READY", lang.hitch(this, "onAllWidgetsReady"));
                //this.alfSubscribe("OE_GET_CASE_WORKFLOW_OPTIONS", lang.hitch(this, "_getWorkFlowSelectControlOptions"));
                this.alfSubscribe(this.startCaseWorkflowNavigationTopic, lang.hitch(this, "_navigateToWorkflowPage"));

                //this._getLoggedInUser();
                //this._getCaseWorkflows();//Get the lis of all case constraints

                // Don't do anything when the widgets are ready
                // This is overwritten when the case info is loaded
                this._allWidgetsProcessedFunction = function () {};
            },

            _navigateToWorkflowPage: function (payload){
                console.log("caseWorkflowService (64) navigating to page");
                this.alfPublish("ALF_NAVIGATE_TO_PAGE", {
                        type: "CONTEXT_RELATIVE",
                        target: "CURRENT",
                        url: "page/start-workflow?targetCase="+payload.caseNodeRef+"&caseId="+payload.caseId
                    }
                )
            },

            /*
            _showStartWorkflowDialog: function dk_openesdh__showCreateCaseDialog(payload) {
                var publishOnSuccessTopic = this.starWorkflowSuccess;
                this.createCaseDialog = new AlfFormDialog({
                    dialogTitle: this.message("workflow-start.dialog.title"),
                    dialogConfirmationButtonTitle: this.message("workflow-start.dialog.label.button.start"),
                    dialogCancellationButtonTitle: this.message("workflow-start.dialog.label.button.cancel"),
                    formSubmissionTopic: this.startWorkflowTopic,
                    formSubmissionPayload: {
                        publishOnSuccessTopic: publishOnSuccessTopic
                    },
                    widgets: []
                });
                this.createCaseDialog.show();
            },

            onAllWidgetsReady: function (payload) {
                this._allWidgetsProcessedFunction();
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