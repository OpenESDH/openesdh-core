define(["dojo/_base/declare",
        "alfresco/core/Core",
        "alfresco/core/CoreXhr",
        "alfresco/core/NodeUtils",
        "dojo/_base/array",
        "dojo/_base/lang",
        "openesdh/pages/_TopicsMixin",
        "alfresco/dialogs/AlfFormDialog",
        "alfresco/core/NotificationUtils",
        "openesdh/common/widgets/controls/category/CategoryPickerControl"],
    function (declare, AlfCore, CoreXhr, NodeUtils, array, lang, _TopicsMixin, AlfFormDialog, NotificationUtils, CategoryPickerControl) {

        return declare([AlfCore, CoreXhr, _TopicsMixin, NotificationUtils], {

            i18nRequirements: [
                {i18nFile: "./i18n/CaseService.properties"}
            ],

            destinationNodeRef: null,
            caseNodeRef: "",
            caseId: "",

            _allWidgetsReady: 0,

            constructor: function (args) {
                lang.mixin(this, args);
                this.caseId = args.caseId;
                this.caseNodeRef = args.nodeRef;

                // Load the case info
                this._caseInfo(this.caseNodeRef, lang.hitch(this, "_onCaseInfoInitialLoadSuccess"));

                // TODO: Listen for requests for case info
                //this.alfSubscribe(this.CaseInfoTopic, lang.hitch(this, "onCaseInfo"));

                this.alfSubscribe("OPENESDH_JOURNALIZE", lang.hitch(this, "onJournalize"));
                this.alfSubscribe("OPENESDH_UNJOURNALIZE", lang.hitch(this, "onUnJournalize"));

                this.alfSubscribe("ALF_WIDGETS_READY", lang.hitch(this, "onAllWidgetsReady"));







                // Don't do anything when the widgets are ready
                // This is overwritten when the case info is loaded
                this._allWidgetsProcessedFunction = function () {};
            },





            // TODO: Handle requests for case info and respond
            //onCaseInfo: function (payload) {
            //    this._caseInfo(payload.caseId != null ? payload.caseId: payload.nodeRef, lang.hitch(this, "_onCaseInfoSuccess"));
            //},

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
                this._allWidgetsReady++;
                if (this._allWidgetsReady == 2) {
                    this._allWidgetsProcessedFunction();
                }
            },

            _onCaseInfoInitialLoadSuccess: function (response, config) {
                this._allWidgetsProcessedFunction = lang.hitch(this, function () {
                    this.alfPublish(this.CaseInfoTopic, response);
                    this.alfPublish("ALF_UPDATE_PAGE_TITLE", {title: response.properties["cm:title"].value});
                });

                // Due to https://issues.alfresco.com/jira/browse/ACE-1488
                // HACK: This assumes that you have two Page instances on
                // the page ('share-header' and 'page'), which both
                // publish ALF_WIDGETS_READY. We only want to publish the info
                // results when the page is ready. Since there is no way to
                // identify which instance the ALF_WIDGETS_READY publication
                // is coming from, we just assume that the 'page' was the
                // second one.
                if (this._allWidgetsReady == 2) {
                    // If the page widgets were ready before we got the results,
                    // call the function to publish the results now
                    this._allWidgetsProcessedFunction();
                }
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
            }
        });
    });